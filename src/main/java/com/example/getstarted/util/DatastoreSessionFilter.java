package com.example.getstarted.util;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@WebFilter(filterName = "DatastoreSessionFilter", urlPatterns = { "", "/books", "/books/mine", "/create", "/delete", "/login", "/logout", "/oauth2callback",
        "/read", "/update" })
public class DatastoreSessionFilter implements Filter {
    private static Datastore datastore;
    private static KeyFactory keyFactory;
    private static final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS");

    @Override
    public void init(final FilterConfig config) throws ServletException {
        datastore = DatastoreOptions.getDefaultInstance().getService();
        keyFactory = datastore.newKeyFactory().setKind("SessionVariable");
        // Delete all sessions unmodified for over two days
        final DateTime dt = DateTime.now(DateTimeZone.UTC);
        final Query<Entity> query = Query.newEntityQueryBuilder().setKind("SessionVariable")
                .setFilter(PropertyFilter.le("lastModified", dt.minusDays(2).toString(dtf))).build();
        final QueryResults<Entity> resultList = datastore.run(query);
        while (resultList.hasNext()) {
            final Entity stateEntity = resultList.next();
            datastore.delete(stateEntity.getKey());
        }
    }

    @Override
    public void doFilter(final ServletRequest servletReq, final ServletResponse servletResp, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) servletReq;
        final HttpServletResponse resp = (HttpServletResponse) servletResp;

        // Check if the session cookie is there, if not there, make a session cookie using a unique
        // identifier.
        final String sessionId = getCookieValue(req, "bookshelfSessionId");
        if (sessionId.equals("")) {
            final String sessionNum = new BigInteger(130, new SecureRandom()).toString(32);
            final Cookie session = new Cookie("bookshelfSessionId", sessionNum);
            session.setPath("/");
            resp.addCookie(session);
        }

        final Map<String, String> datastoreMap = loadSessionVariables(req);

        chain.doFilter(servletReq, servletResp);

        final HttpSession session = req.getSession(); // Create session map
        final Map<String, String> sessionMap = new HashMap<>();
        final Enumeration<String> attrNames = session.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            final String attrName = attrNames.nextElement();
            sessionMap.put(attrName, (String) session.getAttribute(attrName));
        }

        final MapDifference<String, String> diff = Maps.difference(sessionMap, datastoreMap);
        final Map<String, String> setMap = diff.entriesOnlyOnLeft();
        final Map<String, String> deleteMap = diff.entriesOnlyOnRight();

        setSessionVariables(sessionId, setMap);
        deleteSessionVariables(sessionId, FluentIterable.from(deleteMap.keySet()).toArray(String.class));
    }

    @Override
    public void destroy() {
    }

    protected String getCookieValue(final HttpServletRequest req, final String cookieName) {
        final Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    protected void deleteSessionVariables(final String sessionId, final String... varNames) {
        if (sessionId.equals("")) {
            return;
        }
        final Key key = keyFactory.newKey(sessionId);
        final Transaction transaction = datastore.newTransaction();
        try {
            final Entity stateEntity = transaction.get(key);
            if (stateEntity != null) {
                Entity.Builder builder = Entity.newBuilder(stateEntity);
                final StringBuilder delNames = new StringBuilder();
                for (final String varName : varNames) {
                    delNames.append(varName + " ");
                    builder = builder.remove(varName);
                }
                datastore.update(builder.build());
            }
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    protected void deleteSessionWithValue(final String varName, final String varValue) {
        final Transaction transaction = datastore.newTransaction();
        try {
            final Query<Entity> query = Query.newEntityQueryBuilder().setKind("SessionVariable").setFilter(PropertyFilter.eq(varName, varValue)).build();
            final QueryResults<Entity> resultList = transaction.run(query);
            while (resultList.hasNext()) {
                final Entity stateEntity = resultList.next();
                transaction.delete(stateEntity.getKey());
            }
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    protected void setSessionVariables(final String sessionId, final Map<String, String> setMap) {
        if (sessionId.equals("")) {
            return;
        }
        final Key key = keyFactory.newKey(sessionId);
        final Transaction transaction = datastore.newTransaction();
        final DateTime dt = DateTime.now(DateTimeZone.UTC);
        dt.toString(dtf);
        try {
            final Entity stateEntity = transaction.get(key);
            Entity.Builder seBuilder;
            if (stateEntity == null) {
                seBuilder = Entity.newBuilder(key);
            } else {
                seBuilder = Entity.newBuilder(stateEntity);
            }
            for (final String varName : setMap.keySet()) {
                seBuilder.set(varName, setMap.get(varName));
            }
            transaction.put(seBuilder.set("lastModified", dt.toString(dtf)).build());
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    protected Map<String, String> loadSessionVariables(final HttpServletRequest req) throws ServletException {
        final Map<String, String> datastoreMap = new HashMap<>();
        final String sessionId = getCookieValue(req, "bookshelfSessionId");
        if (sessionId.equals("")) {
            return datastoreMap;
        }
        final Key key = keyFactory.newKey(sessionId);
        final Transaction transaction = datastore.newTransaction();
        try {
            final Entity stateEntity = transaction.get(key);
            final StringBuilder logNames = new StringBuilder();
            if (stateEntity != null) {
                for (final String varName : stateEntity.getNames()) {
                    req.getSession().setAttribute(varName, stateEntity.getString(varName));
                    datastoreMap.put(varName, stateEntity.getString(varName));
                    logNames.append(varName + " ");
                }
            }
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }

        return datastoreMap;
    }
}

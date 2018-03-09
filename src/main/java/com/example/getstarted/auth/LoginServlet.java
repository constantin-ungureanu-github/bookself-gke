package com.example.getstarted.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "login", value = "/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private static final Collection<String> SCOPES = Arrays.asList("email", "profile");
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private GoogleAuthorizationCodeFlow flow;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        final String state = new BigInteger(130, new SecureRandom()).toString(32);
        req.getSession().setAttribute("state", state);

        if (req.getAttribute("loginDestination") != null) {
            req.getSession().setAttribute("loginDestination", req.getAttribute("loginDestination"));
            logger.log(Level.INFO, "logging destination " + (String) req.getAttribute("loginDestination"));
        } else {
            req.getSession().setAttribute("loginDestination", "/books");
            logger.log(Level.INFO, "logging destination /books");
        }

        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, getServletContext().getInitParameter("bookshelf.clientID"),
                getServletContext().getInitParameter("bookshelf.clientSecret"), SCOPES).build();

        final String url = flow.newAuthorizationUrl().setRedirectUri(getServletContext().getInitParameter("bookshelf.callback")).setState(state).build();
        resp.sendRedirect(url);
    }
}

package com.example.getstarted.auth;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter(filterName = "ListByUserFilter", value = "/books/mine")
public class ListByUserFilter implements Filter {
    private static final Logger logger = Logger.getLogger(ListByUserFilter.class.getName());

    @Override
    public void init(final FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest servletReq, final ServletResponse servletResp, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) servletReq;
        final HttpServletResponse resp = (HttpServletResponse) servletResp;

        final String instanceId = System.getenv().containsKey("GAE_MODULE_INSTANCE") ? System.getenv("GAE_MODULE_INSTANCE") : "-1";
        logger.log(Level.INFO, "ListByUserFilter processing new request for path: " + req.getRequestURI() + " and instance: " + instanceId);

        if (req.getSession().getAttribute("token") == null && req.getSession().getAttribute("state") == null) {
            logger.log(Level.INFO, "token not detected, setting loginDestination to /books/mine");
            req.setAttribute("loginDestination", "/books/mine");
            resp.sendRedirect("/login");
        } else {
            chain.doFilter(servletReq, servletResp);
        }
    }

    @Override
    public void destroy() {
        logger.log(Level.INFO, "ListByUserFilter is de-initializing");
    }
}

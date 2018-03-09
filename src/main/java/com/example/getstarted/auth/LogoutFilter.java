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

@WebFilter(filterName = "LogoutFilter", urlPatterns = { "/logout" })
public class LogoutFilter implements Filter {
    private static final Logger logger = Logger.getLogger(ListByUserFilter.class.getName());

    @Override
    public void init(final FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest servletReq, final ServletResponse servletResp, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) servletReq;
        final HttpServletResponse resp = (HttpServletResponse) servletResp;
        final String path = req.getRequestURI();

        chain.doFilter(servletReq, servletResp);

        if (path.startsWith("/logout")) {
            resp.sendRedirect("/books");
        }
    }

    @Override
    public void destroy() {
        logger.log(Level.INFO, "destroy called in LogoutFilter");
    }
}

package com.example.getstarted.basicactions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "health", urlPatterns = { "/_ah/health", "/_ah/start", "/_ah/stop" })
public class HealthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(HealthServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        logger.log(Level.INFO, "Got request to my 'ok' servlet for {0}", request.getRequestURI());
        response.setContentType("text/plain");
        final PrintWriter out = response.getWriter();
        out.println("ok");
    }
}

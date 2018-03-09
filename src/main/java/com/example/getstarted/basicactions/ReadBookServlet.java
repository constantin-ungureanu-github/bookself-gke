package com.example.getstarted.basicactions;

import com.example.getstarted.daos.BookDao;
import com.example.getstarted.model.Book;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "read", value = "/read")
public class ReadBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Logger logger = Logger.getLogger(ReadBookServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        final Long id = Long.decode(req.getParameter("id"));
        final BookDao dao = (BookDao) getServletContext().getAttribute("dao");
        try {
            final Book book = dao.readBook(id);

            logger.log(Level.INFO, "Read book with id {0}", id);

            req.setAttribute("book", book);
            req.setAttribute("page", "view");
            req.getRequestDispatcher("/base.jsp").forward(req, resp);
        } catch (final Exception e) {
            throw new ServletException("Error reading book", e);
        }
    }
}

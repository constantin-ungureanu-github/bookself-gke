package com.example.getstarted.basicactions;

import com.example.getstarted.daos.BookDao;
import com.example.getstarted.model.Book;
import com.example.getstarted.model.Result;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "listbyuser", value = "/books/mine")
public class ListByUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ListByUserServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse resp) throws IOException, ServletException {
        final BookDao dao = (BookDao) getServletContext().getAttribute("dao");
        final String startCursor = request.getParameter("cursor");
        List<Book> books = null;
        String endCursor = null;
        try {
            final Result<Book> result = dao.listBooksByUser((String) request.getSession().getAttribute("userId"), startCursor);
            books = result.result;
            endCursor = result.cursor;
        } catch (final Exception e) {
            throw new ServletException("Error listing books", e);
        }

        request.getSession().getServletContext().setAttribute("books", books);
        final StringBuilder bookNames = new StringBuilder();
        for (final Book book : books) {
            bookNames.append(book.getTitle() + " ");
        }

        logger.log(Level.INFO, "Loaded books: " + bookNames.toString() + " for user " + (String) request.getSession().getAttribute("userId"));
        request.getSession().setAttribute("cursor", endCursor);
        request.getSession().setAttribute("page", "list");
        request.getRequestDispatcher("/base.jsp").forward(request, resp);
    }
}

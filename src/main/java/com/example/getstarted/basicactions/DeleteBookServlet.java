package com.example.getstarted.basicactions;

import static java.lang.Long.decode;

import com.example.getstarted.daos.BookDao;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "delete", value = "/delete")
public class DeleteBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final Long id = decode(req.getParameter("id"));
        final BookDao dao = (BookDao) getServletContext().getAttribute("dao");
        try {
            dao.deleteBook(id);
            resp.sendRedirect("/books");
        } catch (final Exception e) {
            throw new ServletException("Error deleting book", e);
        }
    }
}

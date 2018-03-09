package com.example.getstarted.basicactions;

import com.example.getstarted.daos.BookDao;
import com.example.getstarted.model.Book;
import com.example.getstarted.util.CloudStorageHelper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@MultipartConfig
@WebServlet(name = "update", value = "/update")
public class UpdateBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final BookDao dao = (BookDao) getServletContext().getAttribute("dao");
        try {
            final Book book = dao.readBook(Long.decode(req.getParameter("id")));
            req.setAttribute("book", book);
            req.setAttribute("action", "Edit");
            req.setAttribute("destination", "update");
            req.setAttribute("page", "form");
            req.getRequestDispatcher("/base.jsp").forward(req, resp);
        } catch (final Exception e) {
            throw new ServletException("Error loading book for editing", e);
        }
    }

    @Override
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final CloudStorageHelper storageHelper = (CloudStorageHelper) req.getServletContext().getAttribute("storageHelper");
        final String imageUrl = storageHelper.getImageUrl(req, resp, getServletContext().getInitParameter("bookshelf.bucket"));
        final BookDao dao = (BookDao) getServletContext().getAttribute("dao");
        try {

            final Book oldBook = dao.readBook(Long.decode(req.getParameter("id")));

            final Book book = new Book.Builder().author(req.getParameter("author")).createdBy(oldBook.getCreatedBy()).createdById(oldBook.getCreatedById())
                    .description(req.getParameter("description")).id(Long.decode(req.getParameter("id"))).publishedDate(req.getParameter("publishedDate"))
                    .title(req.getParameter("title")).imageUrl(imageUrl).build();
            dao.updateBook(book);
            resp.sendRedirect("/read?id=" + req.getParameter("id"));
        } catch (final Exception e) {
            throw new ServletException("Error updating book", e);
        }
    }
}

package com.example.getstarted.basicactions;

import com.example.getstarted.daos.BookDao;
import com.example.getstarted.model.Book;
import com.example.getstarted.util.CloudStorageHelper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@MultipartConfig
@WebServlet(name = "create", urlPatterns = { "/create" })
public class CreateBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(CreateBookServlet.class.getName());

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("action", "add"); // Part of the Header in form.jsp
        request.setAttribute("destination", "create"); // The urlPattern to invoke (this Servlet)
        request.setAttribute("page", "form"); // Tells base.jsp to include form.jsp
        request.getRequestDispatcher("/base.jsp").forward(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final CloudStorageHelper storageHelper = (CloudStorageHelper) req.getServletContext().getAttribute("storageHelper");
        final String imageUrl = storageHelper.getImageUrl(req, resp, getServletContext().getInitParameter("bookshelf.bucket"));

        String createdByString = "";
        String createdByIdString = "";
        if (req.getSession().getAttribute("token") != null) { // Does the user have a logged in session?
            createdByString = (String) req.getSession().getAttribute("userEmail");
            createdByIdString = (String) req.getSession().getAttribute("userId");
        }

        final BookDao dao = (BookDao) getServletContext().getAttribute("dao");

        final Book book = new Book.Builder().author(req.getParameter("author")).createdBy(createdByString).createdById(createdByIdString)
                .description(req.getParameter("description")).publishedDate(req.getParameter("publishedDate")).title(req.getParameter("title"))
                .imageUrl(imageUrl).build();
        try {
            final Long id = dao.createBook(book);
            logger.log(Level.INFO, "Created book {0}", book);
            resp.sendRedirect("/read?id=" + id.toString()); // read what we just wrote
        } catch (final Exception e) {
            throw new ServletException("Error creating book", e);
        }
    }
}

package com.example.getstarted.basicactions;

import com.example.getstarted.daos.BookDao;
import com.example.getstarted.daos.CloudSqlDao;
import com.example.getstarted.daos.DatastoreDao;
import com.example.getstarted.model.Book;
import com.example.getstarted.model.Result;
import com.example.getstarted.util.CloudStorageHelper;
import com.google.common.base.Strings;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "list", urlPatterns = { "", "/books" }, loadOnStartup = 1)
public class ListBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ListBookServlet.class.getName());

    @Override
    public void init() throws ServletException {
        BookDao dao = null;
        final CloudStorageHelper storageHelper = new CloudStorageHelper();

        final String storageType = getServletContext().getInitParameter("bookshelf.storageType");
        switch (storageType) {
        case "datastore":
            dao = new DatastoreDao();
            break;
        case "cloudsql":
            try {
                String connect = getServletContext().getInitParameter("sql.urlRemote");
                if (connect.contains("localhost")) {
                    connect = getServletContext().getInitParameter("sql.urlLocal");
                }
                dao = new CloudSqlDao(connect);
            } catch (final SQLException e) {
                throw new ServletException("SQL error", e);
            }
            break;
        default:
            throw new IllegalStateException("Invalid storage type. Check if bookshelf.storageType property is set.");
        }
        getServletContext().setAttribute("dao", dao);
        getServletContext().setAttribute("storageHelper", storageHelper);
        getServletContext().setAttribute("isCloudStorageConfigured", !Strings.isNullOrEmpty(getServletContext().getInitParameter("bookshelf.bucket")));
        getServletContext().setAttribute("isAuthConfigured", !Strings.isNullOrEmpty(getServletContext().getInitParameter("bookshelf.clientID")));
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        final BookDao dao = (BookDao) getServletContext().getAttribute("dao");
        final String startCursor = req.getParameter("cursor");
        List<Book> books = null;
        String endCursor = null;
        try {
            final Result<Book> result = dao.listBooks(startCursor);
            logger.log(Level.INFO, "Retrieved list of all books");
            books = result.result;
            endCursor = result.cursor;
        } catch (final Exception e) {
            throw new ServletException("Error listing books", e);
        }
        req.getSession().getServletContext().setAttribute("books", books);
        final StringBuilder bookNames = new StringBuilder();
        for (final Book book : books) {
            bookNames.append(book.getTitle() + " ");
        }
        logger.log(Level.INFO, "Loaded books: " + bookNames.toString());
        req.setAttribute("cursor", endCursor);
        req.setAttribute("page", "list");
        req.getRequestDispatcher("/base.jsp").forward(req, resp);
    }
}

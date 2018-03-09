package com.example.getstarted.daos;

import com.example.getstarted.model.Book;
import com.example.getstarted.model.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

public class CloudSqlDao implements BookDao {
    private static final BasicDataSource dataSource = new BasicDataSource();

    /**
     * A data access object for Bookshelf using a Google Cloud SQL server for storage.
     *
     * @param url the url
     * @throws SQLException the SQL exception
     */
    public CloudSqlDao(final String url) throws SQLException {

        dataSource.setUrl(url);
        final String createTableSql = "CREATE TABLE IF NOT EXISTS books5 ( id INT NOT NULL "
                + "AUTO_INCREMENT, author VARCHAR(255), createdBy VARCHAR(255), createdById VARCHAR(255), "
                + "description VARCHAR(255), publishedDate VARCHAR(255), title VARCHAR(255), imageUrl " + "VARCHAR(255), PRIMARY KEY (id))";
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().executeUpdate(createTableSql);
        }
    }

    @Override
    public Long createBook(final Book book) throws SQLException {
        final String createBookString = "INSERT INTO books5 " + "(author, createdBy, createdById, description, publishedDate, title, imageUrl) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
                final PreparedStatement createBookStmt = conn.prepareStatement(createBookString, Statement.RETURN_GENERATED_KEYS)) {
            createBookStmt.setString(1, book.getAuthor());
            createBookStmt.setString(2, book.getCreatedBy());
            createBookStmt.setString(3, book.getCreatedById());
            createBookStmt.setString(4, book.getDescription());
            createBookStmt.setString(5, book.getPublishedDate());
            createBookStmt.setString(6, book.getTitle());
            createBookStmt.setString(7, book.getImageUrl());
            createBookStmt.executeUpdate();
            try (ResultSet keys = createBookStmt.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    @Override
    public Book readBook(final Long bookId) throws SQLException {
        final String readBookString = "SELECT * FROM books5 WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement readBookStmt = conn.prepareStatement(readBookString)) {
            readBookStmt.setLong(1, bookId);
            try (ResultSet keys = readBookStmt.executeQuery()) {
                keys.next();
                return new Book.Builder().author(keys.getString(Book.AUTHOR)).createdBy(keys.getString(Book.CREATED_BY))
                        .createdById(keys.getString(Book.CREATED_BY_ID)).description(keys.getString(Book.DESCRIPTION)).id(keys.getLong(Book.ID))
                        .publishedDate(keys.getString(Book.PUBLISHED_DATE)).title(keys.getString(Book.TITLE)).imageUrl(keys.getString(Book.IMAGE_URL)).build();
            }
        }
    }

    @Override
    public void updateBook(final Book book) throws SQLException {
        final String updateBookString = "UPDATE books5 SET author = ?, createdBy = ?, createdById = ?, "
                + "description = ?, publishedDate = ?, title = ?, imageUrl = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement updateBookStmt = conn.prepareStatement(updateBookString)) {
            updateBookStmt.setString(1, book.getAuthor());
            updateBookStmt.setString(2, book.getCreatedBy());
            updateBookStmt.setString(3, book.getCreatedById());
            updateBookStmt.setString(4, book.getDescription());
            updateBookStmt.setString(5, book.getPublishedDate());
            updateBookStmt.setString(6, book.getTitle());
            updateBookStmt.setString(7, book.getImageUrl());
            updateBookStmt.setLong(8, book.getId());
            updateBookStmt.executeUpdate();
        }
    }

    @Override
    public void deleteBook(final Long bookId) throws SQLException {
        final String deleteBookString = "DELETE FROM books5 WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement deleteBookStmt = conn.prepareStatement(deleteBookString)) {
            deleteBookStmt.setLong(1, bookId);
            deleteBookStmt.executeUpdate();
        }
    }

    @Override
    public Result<Book> listBooks(final String cursor) throws SQLException {
        int offset = 0;
        if (cursor != null && !cursor.equals("")) {
            offset = Integer.parseInt(cursor);
        }
        final String listBooksString = "SELECT SQL_CALC_FOUND_ROWS author, createdBy, createdById, "
                + "description, id, publishedDate, title, imageUrl FROM books5 ORDER BY title ASC " + "LIMIT 10 OFFSET ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement listBooksStmt = conn.prepareStatement(listBooksString)) {
            listBooksStmt.setInt(1, offset);
            final List<Book> resultBooks = new ArrayList<>();
            try (ResultSet rs = listBooksStmt.executeQuery()) {
                while (rs.next()) {
                    final Book book = new Book.Builder().author(rs.getString(Book.AUTHOR)).createdBy(rs.getString(Book.CREATED_BY))
                            .createdById(rs.getString(Book.CREATED_BY_ID)).description(rs.getString(Book.DESCRIPTION)).id(rs.getLong(Book.ID))
                            .publishedDate(rs.getString(Book.PUBLISHED_DATE)).title(rs.getString(Book.TITLE)).imageUrl(rs.getString(Book.IMAGE_URL)).build();
                    resultBooks.add(book);
                }
            }
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT FOUND_ROWS()")) {
                int totalNumRows = 0;
                if (rs.next()) {
                    totalNumRows = rs.getInt(1);
                }
                if (totalNumRows > offset + 10) {
                    return new Result<>(resultBooks, Integer.toString(offset + 10));
                } else {
                    return new Result<>(resultBooks);
                }
            }
        }
    }

    @Override
    public Result<Book> listBooksByUser(final String userId, final String startCursor) throws SQLException {
        int offset = 0;
        if (startCursor != null && !startCursor.equals("")) {
            offset = Integer.parseInt(startCursor);
        }
        final String listBooksString = "SELECT SQL_CALC_FOUND_ROWS author, createdBy, createdById, "
                + "description, id, publishedDate, title, imageUrl FROM books WHERE createdById = ? " + "ORDER BY title ASC LIMIT 10 OFFSET ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement listBooksStmt = conn.prepareStatement(listBooksString)) {
            listBooksStmt.setString(1, userId);
            listBooksStmt.setInt(2, offset);
            final List<Book> resultBooks = new ArrayList<>();
            try (ResultSet rs = listBooksStmt.executeQuery()) {
                while (rs.next()) {
                    final Book book = new Book.Builder().author(rs.getString(Book.AUTHOR)).createdBy(rs.getString(Book.CREATED_BY))
                            .createdById(rs.getString(Book.CREATED_BY_ID)).description(rs.getString(Book.DESCRIPTION)).id(rs.getLong(Book.ID))
                            .publishedDate(rs.getString(Book.PUBLISHED_DATE)).title(rs.getString(Book.TITLE)).imageUrl(rs.getString(Book.IMAGE_URL)).build();
                    resultBooks.add(book);
                }
            }
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT FOUND_ROWS()")) {
                int totalNumRows = 0;
                if (rs.next()) {
                    totalNumRows = rs.getInt(1);
                }
                if (totalNumRows > offset + 10) {
                    return new Result<>(resultBooks, Integer.toString(offset + 10));
                } else {
                    return new Result<>(resultBooks);
                }
            }
        }
    }
}

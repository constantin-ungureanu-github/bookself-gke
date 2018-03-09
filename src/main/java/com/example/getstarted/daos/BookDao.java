package com.example.getstarted.daos;

import com.example.getstarted.model.Book;
import com.example.getstarted.model.Result;

import java.sql.SQLException;

public interface BookDao {
    Long createBook(Book book) throws SQLException;

    Book readBook(Long bookId) throws SQLException;

    void updateBook(Book book) throws SQLException;

    void deleteBook(Long bookId) throws SQLException;

    Result<Book> listBooks(String startCursor) throws SQLException;

    Result<Book> listBooksByUser(String userId, String startCursor) throws SQLException;
}

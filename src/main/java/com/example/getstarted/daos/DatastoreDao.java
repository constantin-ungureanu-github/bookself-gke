package com.example.getstarted.daos;

import com.example.getstarted.model.Book;
import com.example.getstarted.model.Result;
import com.google.cloud.datastore.Cursor;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import java.util.ArrayList;
import java.util.List;

public class DatastoreDao implements BookDao {
    private final Datastore datastore;
    private final KeyFactory keyFactory;

    public DatastoreDao() {
        datastore = DatastoreOptions.getDefaultInstance().getService(); // Authorized Datastore service
        keyFactory = datastore.newKeyFactory().setKind("Book5"); // Is used for creating keys later
    }

    /**
     * Entity to book.
     *
     * @param entity
     *            the entity
     * @return the book
     */
    public Book entityToBook(final Entity entity) {
        return new Book.Builder().author(entity.getString(Book.AUTHOR)).description(entity.getString(Book.DESCRIPTION)).id(entity.getKey().getId())
                .publishedDate(entity.getString(Book.PUBLISHED_DATE)).title(entity.getString(Book.TITLE))
                .imageUrl(entity.contains(Book.IMAGE_URL) ? entity.getString(Book.IMAGE_URL) : null).build();
    }

    @Override
    public Long createBook(final Book book) {
        final IncompleteKey key = keyFactory.newKey(); // Key will be assigned once written
        final FullEntity<IncompleteKey> incBookEntity = FullEntity.newBuilder(key) // Create the Entity
                .set(Book.AUTHOR, book.getAuthor()) // Add Property ("author", book.getAuthor())
                .set(Book.DESCRIPTION, book.getDescription()).set(Book.PUBLISHED_DATE, book.getPublishedDate()).set(Book.TITLE, book.getTitle())
                .set(Book.IMAGE_URL, book.getImageUrl()).build();
        final Entity bookEntity = datastore.add(incBookEntity); // Save the Entity
        return bookEntity.getKey().getId(); // The ID of the Key
    }

    @Override
    public Book readBook(final Long bookId) {
        final Entity bookEntity = datastore.get(keyFactory.newKey(bookId)); // Load an Entity for Key(id)
        return entityToBook(bookEntity);
    }

    @Override
    public void updateBook(final Book book) {
        final Key key = keyFactory.newKey(book.getId()); // From a book, create a Key
        final Entity entity = Entity.newBuilder(key) // Convert Book to an Entity
                .set(Book.AUTHOR, book.getAuthor()).set(Book.DESCRIPTION, book.getDescription()).set(Book.PUBLISHED_DATE, book.getPublishedDate())
                .set(Book.TITLE, book.getTitle()).set(Book.IMAGE_URL, book.getImageUrl()).build();
        datastore.update(entity); // Update the Entity
    }

    @Override
    public void deleteBook(final Long bookId) {
        final Key key = keyFactory.newKey(bookId);
        datastore.delete(key);
    }

    /**
     * Entities to books.
     *
     * @param resultList
     *            the result list
     * @return the list
     */
    public List<Book> entitiesToBooks(final QueryResults<Entity> resultList) {
        final List<Book> resultBooks = new ArrayList<>();
        while (resultList.hasNext()) {
            resultBooks.add(entityToBook(resultList.next()));
        }
        return resultBooks;
    }

    @Override
    public Result<Book> listBooks(final String startCursorString) {
        Cursor startCursor = null;
        if (startCursorString != null && !startCursorString.equals("")) {
            startCursor = Cursor.fromUrlSafe(startCursorString);
        }
        final Query<Entity> query = Query.newEntityQueryBuilder().setKind("Book5").setLimit(10).setStartCursor(startCursor).setOrderBy(OrderBy.asc(Book.TITLE))
                .build();
        final QueryResults<Entity> resultList = datastore.run(query);
        final List<Book> resultBooks = entitiesToBooks(resultList);
        final Cursor cursor = resultList.getCursorAfter();
        if (cursor != null && resultBooks.size() == 10) {
            final String cursorString = cursor.toUrlSafe();
            return new Result<>(resultBooks, cursorString);
        } else {
            return new Result<>(resultBooks);
        }
    }

    @Override
    public Result<Book> listBooksByUser(final String userId, final String startCursorString) {
        Cursor startCursor = null;
        if (startCursorString != null && !startCursorString.equals("")) {
            startCursor = Cursor.fromUrlSafe(startCursorString);
        }

        final Query<Entity> query = Query.newEntityQueryBuilder().setKind("Book5").setFilter(PropertyFilter.eq(Book.CREATED_BY_ID, userId)).setLimit(10)
                .setStartCursor(startCursor).setOrderBy(OrderBy.asc(Book.TITLE)).build();
        final QueryResults<Entity> resultList = datastore.run(query);
        final List<Book> resultBooks = entitiesToBooks(resultList);
        final Cursor cursor = resultList.getCursorAfter();
        if (cursor != null && resultBooks.size() == 10) {
            final String cursorString = cursor.toUrlSafe();
            return new Result<>(resultBooks, cursorString);
        } else {
            return new Result<>(resultBooks);
        }
    }
}

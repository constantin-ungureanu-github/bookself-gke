package com.example.getstarted.model;

public class Book {
    public static final String ID = "id";
    public static final String AUTHOR = "author";
    public static final String CREATED_BY = "createdBy";
    public static final String CREATED_BY_ID = "createdById";
    public static final String DESCRIPTION = "description";
    public static final String PUBLISHED_DATE = "publishedDate";
    public static final String TITLE = "title";
    public static final String IMAGE_URL = "imageUrl";

    private final Long id;
    private final String title;
    private final String author;
    private final String createdBy;
    private final String createdById;
    private final String publishedDate;
    private final String description;
    private final String imageUrl;

    private Book(final Builder builder) {
        title = builder.title;
        author = builder.author;
        createdBy = builder.createdBy;
        createdById = builder.createdById;
        publishedDate = builder.publishedDate;
        description = builder.description;
        id = builder.id;
        imageUrl = builder.imageUrl;
    }

    public static class Builder {
        private String title;
        private String author;
        private String createdBy;
        private String createdById;
        private String publishedDate;
        private String description;
        private Long id;
        private String imageUrl;

        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        public Builder author(final String author) {
            this.author = author;
            return this;
        }

        public Builder createdBy(final String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdById(final String createdById) {
            this.createdById = createdById;
            return this;
        }

        public Builder publishedDate(final String publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder id(final Long id) {
            this.id = id;
            return this;
        }

        public Builder imageUrl(final String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedById() {
        return createdById;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "Title: " + title + ", Author: " + author + ", Published date: " + publishedDate + ", Added by: " + createdBy;
    }
}

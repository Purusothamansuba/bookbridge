package com.bookbridge.model;

import java.io.Serializable;
import java.util.Objects;

public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    private int bookId;
    private String title;
    private String author;
    private int availableCopies;
    private int branchId;
    private String branchName;
    private String category;

    public Book() {}

    public Book(int bookId, String title, String author, int availableCopies, int branchId) {
        this(bookId, title, author, availableCopies, branchId, "General");
    }

    public Book(int bookId, String title, String author, int availableCopies, int branchId, String category) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.availableCopies = availableCopies;
        this.branchId = branchId;
        this.category = (category != null && !category.isEmpty()) ? category : "General";
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return bookId == book.bookId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return String.format(
            "Book ID: %d | Title: %s | Author: %s | Copies: %d | Branch: %s (%d)",
            bookId, title, author, availableCopies, 
            (branchName != null ? branchName : "Branch " + branchId), branchId
        );
    }
}

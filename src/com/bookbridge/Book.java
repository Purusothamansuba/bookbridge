package com.bookbridge;

public class Book {

    private int bookId;
    private String title;
    private String author;
    private int availableCopies;
    private int branchId;

    public Book(int bookId, String title, String author, int availableCopies, int branchId) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.availableCopies = availableCopies;
        this.branchId = branchId;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    @Override
    public String toString() {
        return "Book ID : " + bookId +
               "\nTitle : " + title +
               "\nAuthor : " + author +
               "\nAvailable Copies : " + availableCopies;
    }
}
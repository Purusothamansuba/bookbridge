package com.bookbridge.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurchaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String bookName;
    private String author;
    private String requesterName;
    private String status; // PENDING, APPROVED, ORDERED, REJECTED
    private String requestDate;

    public PurchaseRequest() {
        this.status = "PENDING";
        this.requestDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }

    public PurchaseRequest(String bookName) {
        this(0, bookName, "Unknown Author", "Member", "PENDING", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
    }

    public PurchaseRequest(int id, String bookName, String author, String requesterName, String status, String requestDate) {
        this.id = id;
        this.bookName = bookName;
        this.author = author != null ? author : "Unknown Author";
        this.requesterName = requesterName != null ? requesterName : "Member";
        this.status = status != null ? status : "PENDING";
        this.requestDate = requestDate != null ? requestDate : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    @Override
    public String toString() {
        return String.format(
            "Purchase Request #%d: '%s' by %s | Requested By: %s | Status: %s | Date: %s",
            id, bookName, author, requesterName, status, requestDate
        );
    }
}

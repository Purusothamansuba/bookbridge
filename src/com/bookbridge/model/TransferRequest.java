package com.bookbridge.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransferRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String bookName;
    private String fromBranch;
    private String toBranch;
    private String requesterName;
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED
    private String requestDate;

    public TransferRequest() {
        this.status = "PENDING";
        this.requestDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }

    public TransferRequest(String bookName, String fromBranch, String toBranch) {
        this(0, bookName, fromBranch, toBranch, "Member", "PENDING", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
    }

    public TransferRequest(int id, String bookName, String fromBranch, String toBranch, String requesterName, String status, String requestDate) {
        this.id = id;
        this.bookName = bookName;
        this.fromBranch = fromBranch;
        this.toBranch = toBranch;
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

    public String getFromBranch() {
        return fromBranch;
    }

    public void setFromBranch(String fromBranch) {
        this.fromBranch = fromBranch;
    }

    public String getToBranch() {
        return toBranch;
    }

    public void setToBranch(String toBranch) {
        this.toBranch = toBranch;
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
            "Transfer Request #%d: '%s' | From: %s -> To: %s | Status: %s | Date: %s",
            id, bookName, fromBranch, toBranch, status, requestDate
        );
    }
}

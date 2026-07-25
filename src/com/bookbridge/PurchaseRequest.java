package com.bookbridge;

public class PurchaseRequest {

    private String bookName;

    public PurchaseRequest(String bookName) {
        this.bookName = bookName;
    }

    @Override
    public String toString() {
        return "Requested Book : " + bookName;
    }
}

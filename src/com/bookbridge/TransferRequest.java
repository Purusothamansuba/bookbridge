package com.bookbridge;

public class TransferRequest {

    private String bookName;
    private String fromBranch;
    private String toBranch;

    public TransferRequest(String bookName, String fromBranch, String toBranch) {
        this.bookName = bookName;
        this.fromBranch = fromBranch;
        this.toBranch = toBranch;
    }

    @Override
    public String toString() {
        return "Book : " + bookName +
               "\nFrom : " + fromBranch +
               "\nTo : " + toBranch;
    }
}
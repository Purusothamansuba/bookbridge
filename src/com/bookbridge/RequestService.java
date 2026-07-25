package com.bookbridge;

import java.util.ArrayList;

public class RequestService {

    public static ArrayList<TransferRequest> transferRequests = new ArrayList<>();
    public static ArrayList<PurchaseRequest> purchaseRequests = new ArrayList<>();

    public static void addTransferRequest(String bookName, String from, String to) {

        transferRequests.add(new TransferRequest(bookName, from, to));

        System.out.println("\nTransfer Request Submitted Successfully!");
    }

    public static void addPurchaseRequest(String bookName) {

        purchaseRequests.add(new PurchaseRequest(bookName));

        System.out.println("\nPurchase Request Submitted Successfully!");
    }

    public static void viewTransferRequests() {

        System.out.println("\n========== TRANSFER REQUESTS ==========");

        if (transferRequests.isEmpty()) {
            System.out.println("No Transfer Requests.");
            return;
        }

        for (TransferRequest request : transferRequests) {
            System.out.println(request);
            System.out.println("--------------------------");
        }
    }

    public static void viewPurchaseRequests() {

        System.out.println("\n========== PURCHASE REQUESTS ==========");

        if (purchaseRequests.isEmpty()) {
            System.out.println("No Purchase Requests.");
            return;
        }

        for (PurchaseRequest request : purchaseRequests) {
            System.out.println(request);
            System.out.println("--------------------------");
        }
    }
}
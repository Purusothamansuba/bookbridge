package com.bookbridge.server;

import com.bookbridge.model.Book;
import com.bookbridge.model.Branch;
import com.bookbridge.model.PurchaseRequest;
import com.bookbridge.model.TransferRequest;
import com.bookbridge.network.NetworkMessage;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String clientIp = socket.getInetAddress().getHostAddress();
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            out.flush();
            while (!socket.isClosed()) {
                NetworkMessage request;
                try {
                    request = (NetworkMessage) in.readObject();
                } catch (EOFException | SocketException e) {
                    break; // Client disconnected normally
                }

                if (request == null) break;

                NetworkMessage response = processRequest(request);
                out.writeObject(response);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("[ClientHandler] Notice from " + clientIp + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }

    private NetworkMessage processRequest(NetworkMessage req) {
        if (req == null || req.action == null) {
            return NetworkMessage.error("UNKNOWN", "Invalid request payload.");
        }

        try {
            switch (req.action) {
                case "VIEW_ALL_BOOKS": {
                    List<Book> books = DatabaseConnection.getAllBooks();
                    return NetworkMessage.success(req.action, books);
                }

                case "SEARCH_BOOK": {
                    String query = "";
                    Integer branchId = null;
                    if (req.payload instanceof Object[]) {
                        Object[] arr = (Object[]) req.payload;
                        if (arr.length > 0 && arr[0] != null) query = (String) arr[0];
                        if (arr.length > 1 && arr[1] != null) branchId = (Integer) arr[1];
                    } else if (req.payload instanceof String) {
                        query = (String) req.payload;
                    }
                    List<Book> results = DatabaseConnection.searchBooks(query, branchId);
                    return NetworkMessage.success(req.action, results);
                }

                case "BORROW_BOOK": {
                    Object[] borrowData = (Object[]) req.payload;
                    int bookId = (int) borrowData[0];
                    int userBranchId = (int) borrowData[1];
                    String resultMsg = DatabaseConnection.borrowBook(bookId, userBranchId);
                    return NetworkMessage.success(req.action, resultMsg);
                }

                case "RETURN_BOOK": {
                    Object[] returnData = (Object[]) req.payload;
                    int bookId = (int) returnData[0];
                    int userBranchId = (int) returnData[1];
                    DatabaseConnection.returnBook(bookId, userBranchId);
                    return NetworkMessage.success(req.action, "Book returned successfully!");
                }

                case "ADD_BOOK": {
                    Book book = (Book) req.payload;
                    DatabaseConnection.addBook(book);
                    return NetworkMessage.success(req.action, "Book added successfully!");
                }

                case "DELETE_BOOK": {
                    int bookId = (int) req.payload;
                    DatabaseConnection.deleteBook(bookId);
                    return NetworkMessage.success(req.action, "Book removed successfully!");
                }

                case "UPDATE_BOOK": {
                    Book book = (Book) req.payload;
                    DatabaseConnection.updateBook(book);
                    return NetworkMessage.success(req.action, "Book updated successfully!");
                }

                case "GET_BRANCHES": {
                    List<Branch> branches = DatabaseConnection.getBranches();
                    return NetworkMessage.success(req.action, branches);
                }

                case "GET_BRANCH_NAME": {
                    int branchId = (int) req.payload;
                    String name = DatabaseConnection.getBranchName(branchId);
                    return NetworkMessage.success(req.action, name);
                }

                case "ADD_TRANSFER_REQUEST": {
                    TransferRequest tr;
                    if (req.payload instanceof TransferRequest) {
                        tr = (TransferRequest) req.payload;
                    } else {
                        Object[] arr = (Object[]) req.payload;
                        tr = new TransferRequest((String) arr[0], (String) arr[1], (String) arr[2]);
                    }
                    DatabaseConnection.addTransferRequest(tr);
                    return NetworkMessage.success(req.action, "Transfer request submitted.");
                }

                case "VIEW_TRANSFER_REQUESTS": {
                    List<TransferRequest> list = DatabaseConnection.getTransferRequests();
                    return NetworkMessage.success(req.action, list);
                }

                case "UPDATE_TRANSFER_STATUS": {
                    Object[] arr = (Object[]) req.payload;
                    int id = (int) arr[0];
                    String status = (String) arr[1];
                    DatabaseConnection.updateTransferStatus(id, status);
                    return NetworkMessage.success(req.action, "Transfer request updated to " + status);
                }

                case "ADD_PURCHASE_REQUEST": {
                    PurchaseRequest pr;
                    if (req.payload instanceof PurchaseRequest) {
                        pr = (PurchaseRequest) req.payload;
                    } else if (req.payload instanceof Object[]) {
                        Object[] arr = (Object[]) req.payload;
                        pr = new PurchaseRequest(0, (String) arr[0], (String) arr[1], (String) arr[2], "PENDING", null);
                    } else {
                        pr = new PurchaseRequest((String) req.payload);
                    }
                    DatabaseConnection.addPurchaseRequest(pr);
                    return NetworkMessage.success(req.action, "Purchase request submitted.");
                }

                case "VIEW_PURCHASE_REQUESTS": {
                    List<PurchaseRequest> list = DatabaseConnection.getPurchaseRequests();
                    return NetworkMessage.success(req.action, list);
                }

                case "UPDATE_PURCHASE_STATUS": {
                    Object[] arr = (Object[]) req.payload;
                    int id = (int) arr[0];
                    String status = (String) arr[1];
                    DatabaseConnection.updatePurchaseStatus(id, status);
                    return NetworkMessage.success(req.action, "Purchase request updated to " + status);
                }

                case "GET_STATISTICS": {
                    Map<String, Object> stats = DatabaseConnection.getSystemStatistics();
                    return NetworkMessage.success(req.action, stats);
                }

                default:
                    return NetworkMessage.error(req.action, "Unknown action: " + req.action);
            }
        } catch (Exception e) {
            return NetworkMessage.error(req.action, e.getMessage());
        }
    }
}

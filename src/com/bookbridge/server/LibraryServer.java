package com.bookbridge.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LibraryServer {

    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 50;
    private static volatile boolean running = true;
    private static ServerSocket serverSocket;
    private static ExecutorService threadPool;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" 📚 Starting BookBridge Core Socket Server (Port " + PORT + ")");
        System.out.println("=================================================");

        // Pre-initialize database connection / memory fallback
        try {
            Class.forName("com.bookbridge.server.DatabaseConnection");
        } catch (Exception ignored) {}

        threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Register graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Shutting down BookBridge Server...");
            running = false;
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException ignored) {}

            if (threadPool != null) {
                threadPool.shutdown();
                try {
                    if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                        threadPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    threadPool.shutdownNow();
                }
            }
            DatabaseConnection.closeConnection();
            System.out.println("[Server] BookBridge Server stopped cleanly.");
        }));

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("🚀 [Server] Socket Server is listening on port " + PORT + " with Thread Pool [" + THREAD_POOL_SIZE + " workers].");
            System.out.println("⚡ Ready to accept client connections...\n");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setTcpNoDelay(true);
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (!running) break;
                    System.err.println("[Server] Socket Accept Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("❌ [Server] Failed to bind to port " + PORT + ": " + e.getMessage());
        }
    }
}

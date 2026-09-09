package com.bookbridge.client;

import com.bookbridge.network.NetworkMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8080;

    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    public static synchronized void connect() throws IOException {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            socket = new Socket(SERVER_ADDRESS, PORT);
            socket.setTcpNoDelay(true);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        }
    }

    public static synchronized NetworkMessage sendRequest(NetworkMessage request) {
        int attempts = 0;
        while (attempts < 2) {
            try {
                connect();
                out.writeObject(request);
                out.flush();
                out.reset(); // Clear object cache so modified objects are properly re-serialized
                return (NetworkMessage) in.readObject();
            } catch (Exception e) {
                attempts++;
                disconnect();
                if (attempts >= 2) {
                    return NetworkMessage.error(request.action, "Network error: " + e.getMessage());
                }
            }
        }
        return NetworkMessage.error(request.action, "Failed to reach server after retrying.");
    }

    public static synchronized void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {}
        socket = null;
        out = null;
        in = null;
    }
}

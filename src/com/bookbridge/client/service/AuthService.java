package com.bookbridge.client.service;

import com.bookbridge.client.NetworkClient;
import com.bookbridge.model.User;
import com.bookbridge.network.NetworkMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {

    // Thread-safe Session Store mapping Session Token -> Logged-in User
    private static final Map<String, User> activeSessions = new ConcurrentHashMap<>();
    private static User cliCurrentUser = null;

    /**
     * Authenticates credentials with backend server and registers a web session token
     */
    public static String createWebSession(String username, String password) throws Exception {
        User user = authenticate(username, password);
        String sessionToken = UUID.randomUUID().toString();
        activeSessions.put(sessionToken, user);
        return sessionToken;
    }

    public static User getUserBySessionToken(String token) {
        if (token == null || token.isEmpty()) return null;
        return activeSessions.get(token);
    }

    public static void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }

    // CLI terminal authentication
    public static User login(String username, String password) throws Exception {
        cliCurrentUser = authenticate(username, password);
        return cliCurrentUser;
    }

    public static User loginCLI(String username, String password) throws Exception {
        return login(username, password);
    }

    public static User getCliCurrentUser() {
        return cliCurrentUser;
    }

    public static void logout() {
        cliCurrentUser = null;
    }

    public static void logoutCLI() {
        logout();
    }

    // Core backend authentication call
    public static User authenticate(String username, String password) throws Exception {
        NetworkMessage req = new NetworkMessage("AUTHENTICATE_USER", new Object[]{username, password});
        NetworkMessage res = NetworkClient.sendRequest(req);
        if (res.success && res.responseData instanceof User) {
            return (User) res.responseData;
        }
        throw new Exception(res.errorMessage != null ? res.errorMessage : "Invalid username or password.");
    }

    public static void createUser(User user) throws Exception {
        NetworkMessage req = new NetworkMessage("CREATE_USER", user);
        NetworkMessage res = NetworkClient.sendRequest(req);
        if (!res.success) {
            throw new Exception(res.errorMessage != null ? res.errorMessage : "Failed to create user.");
        }
    }

    @SuppressWarnings("unchecked")
    public static List<User> fetchAllUsers() {
        NetworkMessage req = new NetworkMessage("GET_ALL_USERS", null);
        NetworkMessage res = NetworkClient.sendRequest(req);
        if (res.success && res.responseData instanceof List) {
            return (List<User>) res.responseData;
        }
        return new ArrayList<>();
    }

    public static void deleteUser(int userId) throws Exception {
        NetworkMessage req = new NetworkMessage("DELETE_USER", userId);
        NetworkMessage res = NetworkClient.sendRequest(req);
        if (!res.success) {
            throw new Exception(res.errorMessage != null ? res.errorMessage : "Failed to delete user.");
        }
    }
}

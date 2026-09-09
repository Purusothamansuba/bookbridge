package com.bookbridge.client.service;

import com.bookbridge.client.NetworkClient;
import com.bookbridge.model.User;
import com.bookbridge.network.NetworkMessage;

import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private static User currentUser = null;

    public static User login(String username, String password) throws Exception {
        NetworkMessage req = new NetworkMessage("AUTHENTICATE_USER", new Object[]{username, password});
        NetworkMessage res = NetworkClient.sendRequest(req);
        if (res.success && res.responseData instanceof User) {
            currentUser = (User) res.responseData;
            return currentUser;
        }
        throw new Exception(res.errorMessage != null ? res.errorMessage : "Invalid username or password.");
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
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

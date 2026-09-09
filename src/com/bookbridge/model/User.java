package com.bookbridge.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role; // ADMIN or MEMBER
    private int branchId;
    private String branchName;
    private String createdAt;

    public User() {
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }

    public User(int userId, String username, String password, String fullName, String role, int branchId) {
        this(userId, username, password, fullName, role, branchId, null, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
    }

    public User(int userId, String username, String password, String fullName, String role, int branchId, String branchName, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role != null ? role.toUpperCase() : "MEMBER";
        this.branchId = branchId;
        this.branchName = branchName;
        this.createdAt = createdAt != null ? createdAt : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = (role != null) ? role.toUpperCase() : "MEMBER";
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId || Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    @Override
    public String toString() {
        return String.format(
            "User[ID: %d | Username: %s | Name: %s | Role: %s | Branch: %s (%d)]",
            userId, username, fullName, role,
            (branchName != null ? branchName : "Branch " + branchId), branchId
        );
    }
}

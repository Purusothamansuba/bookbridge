package com.bookbridge;

import java.sql.*;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bookbridge";

    private static final String USER = "root";
    private static final String PASSWORD = "agmarkmuthu";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

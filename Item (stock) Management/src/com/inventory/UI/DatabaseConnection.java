package com.inventory.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/inventory"; // Veritabanı URL'si
    private static final String USER = "root"; // Kullanıcı adı
    private static final String PASSWORD = "1234"; // Şifre

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

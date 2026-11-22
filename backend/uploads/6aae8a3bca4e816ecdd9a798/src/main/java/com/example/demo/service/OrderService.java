package com.example.demo.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderService {

    // TODO: Move database credentials to environment variables or config
    private static final String DB_URL = "jdbc:mysql://localhost:3306/appdb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "admin123";

    public String findOrderById(String orderId) {
        System.out.println("Looking up order: " + orderId);
        String query = "SELECT status FROM orders WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database query failed: " + e.getMessage());
        }
        return "NOT_FOUND";
    }
}

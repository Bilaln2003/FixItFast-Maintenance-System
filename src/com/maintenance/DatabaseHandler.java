package com.maintenance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

    // ==========================================
    // FINAL CLOUD CREDENTIALS
    // ==========================================
    private static final String HOST = "sql12.freesqldatabase.com";
    private static final String DB_NAME = "sql12811823";
    private static final String USER_NAME = "sql12811823";
    private static final String PASSWORD = "PS46aFw7nx"; // Your real passwordF

    // Connection URL
    private static final String URL = "jdbc:mysql://" + HOST + ":3306/" + DB_NAME;

    private static Connection getDBConnection() throws SQLException, ClassNotFoundException {
        // Load MySQL Driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER_NAME, PASSWORD);
    }

    public static void initDB() {
        try (Connection conn = getDBConnection();
             Statement stmt = conn.createStatement()) {

            // Create table if it doesn't exist
            String sql = "CREATE TABLE IF NOT EXISTS requests (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "tenant_name VARCHAR(255)," +
                    "address VARCHAR(255)," +
                    "issue TEXT," +
                    "category VARCHAR(100)," +
                    "severity INT," +
                    "status VARCHAR(50))";

            stmt.execute(sql);
            System.out.println("SUCCESS: Connected to Cloud Database!");

        } catch (Exception e) {
            System.out.println("CLOUD CONNECTION ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveRequest(Request req) {
        String sql = "INSERT INTO requests(tenant_name, address, issue, category, severity, status) VALUES(?,?,?,?,?,?)";

        try (Connection conn = getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, req.getTenantName());
            pstmt.setString(2, req.getAddress());
            pstmt.setString(3, req.getIssue());
            pstmt.setString(4, req.getCategory());
            pstmt.setInt(5, req.getSeverity());
            pstmt.setString(6, req.getStatus());
            pstmt.executeUpdate();
            System.out.println("Request saved to Cloud!");

        } catch (Exception e) {
            System.out.println("Save Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Request> loadRequests() {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT * FROM requests WHERE status = 'PENDING'";

        try (Connection conn = getDBConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Request(
                        rs.getInt("id"),
                        rs.getString("tenant_name"),
                        rs.getString("address"),
                        rs.getString("issue"),
                        rs.getString("category"),
                        rs.getInt("severity"),
                        rs.getString("status")
                ));
            }
            System.out.println("Synced " + list.size() + " requests from Cloud.");

        } catch (Exception e) {
            System.out.println("Load Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public static void markRequestCompleted(int id) {
        String sql = "UPDATE requests SET status = 'COMPLETED' WHERE id = ?";

        try (Connection conn = getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Job " + id + " Completed.");

        } catch (Exception e) {
            System.out.println("Update Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
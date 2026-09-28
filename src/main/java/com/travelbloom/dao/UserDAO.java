package com.travelbloom.dao;

import com.travelbloom.database.Database;
import com.travelbloom.model.User;
import com.travelbloom.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    public UserDAO() {
        createTable();
    }

    private void createTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    email TEXT,
                    password_hash TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // REGISTER — returns null on success,
    // or an error message string on failure
    // =========================================

    public String register(String username, String email, String rawPassword) {

        if (usernameExists(username)) {
            return "That username is already taken.";
        }

        String sql = """
                INSERT INTO users (username, email, password_hash)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, PasswordUtil.hash(rawPassword));

            stmt.executeUpdate();

            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Could not create the account. Please try again.";
        }
    }


    // =========================================
    // AUTHENTICATE
    // =========================================

    public User authenticate(String username, String rawPassword) {

        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String storedHash = rs.getString("password_hash");

                if (PasswordUtil.matches(rawPassword, storedHash)) {

                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            storedHash
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // =========================================
    // USERNAME EXISTS?
    // =========================================

    public boolean usernameExists(String username) {

        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
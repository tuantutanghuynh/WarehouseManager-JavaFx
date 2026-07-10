package com.warehousemanager.repositories;

import java.sql.*;

import com.warehousemanager.config.DatabaseConfig;
import com.warehousemanager.models.entity.User;

// DAO for the Users table — every SQL statement for reading/writing user
// accounts lives here. Same connection-per-call pattern as GoodsRepository:
// each method opens its own Connection via DatabaseConfig.getConnection()
// and closes it via try-with-resources, so calls from different threads
// never share the same Connection.
public class UserRepository {

    // Look up a user by username; returns null if not found.
    public User findByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE Username = ? ";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Used by AuthService.register() to reject duplicate usernames before insert.
    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    // Insert a newly registered user (passwordHash/salt already computed by
    // caller)
    public boolean insert(User u) {
        String sql = "INSERT INTO Users(Username, PasswordHash, Salt, Email, Role, Status) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getSalt());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getRole());
            ps.setBoolean(6, u.isStatus());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Block/unblock a user account (admin action, not wired to UI yet in this
    // project)
    public boolean updateStatus(int userId, boolean status) {
        String sql = "UPDATE Users SET Status = ? WHERE UserId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Map one Users row into a User entity
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("UserId"));
        u.setUsername(rs.getString("Username"));
        u.setPasswordHash(rs.getString("PasswordHash"));
        u.setSalt(rs.getString("Salt"));
        u.setEmail(rs.getString("Email"));
        u.setRole(rs.getString("Role"));
        u.setStatus(rs.getBoolean("Status"));
        return u;

    }
}

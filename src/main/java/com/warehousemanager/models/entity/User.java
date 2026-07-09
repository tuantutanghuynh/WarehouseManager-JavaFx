package com.warehousemanager.models.entity;

// Login account entity, mapped 1:1 with a row in the Users table.
// passwordHash/salt are never plain text — see PasswordHasher for how they're
// produced. role ("admin"/"user") and status (active/blocked) drive the
// authorization checks done in UserSession/controllers.
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String salt;
    private String email;
    private String role;
    private boolean status;

    // Auto-increment primary key from the Users table.
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    // Unique login name.
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Salted SHA-256 hash of the password — never the raw password.
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Random per-user salt mixed into the password before hashing.
    public String getSalt() {
        return salt;
    }
    public void setSalt(String salt) {
        this.salt = salt;
    }

    // Optional contact email.
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // "admin" or "user" — checked by UserSession.isAdmin() for authorization.
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    // true = active, false = blocked from logging in.
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
}

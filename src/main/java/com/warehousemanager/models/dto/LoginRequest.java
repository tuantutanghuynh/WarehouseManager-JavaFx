package com.warehousemanager.models.dto;

// Immutable DTO carrying login input from LoginController to AuthService.
// A Java record auto-generates the constructor, accessors (username()/password()),
// equals/hashCode/toString — no setters needed since this data never changes
// after being created.
public record LoginRequest(String username, String password) {
}

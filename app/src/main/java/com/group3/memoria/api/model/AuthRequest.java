package com.group3.memoria.api.model;

public class AuthRequest {
    private String username;
    private String password;

    // Getters and setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
}

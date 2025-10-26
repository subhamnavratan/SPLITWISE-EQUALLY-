package com.Zeta.SPLITWISE.EQUALLY.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
// import java.util.List; // Removed unnecessary import if it was present

@Document(collection = "_USERS")
public class User {
    @Id
    private String userId;

    private String name;

    @Indexed(unique = true)
    private Long phone;

    @Indexed(unique = true)
    private String email;

    // NEW FIELD: To store the hashed password for authentication
    private String password;

    // =================================================================
    // 1. Constructors (Updated to include password)
    // =================================================================

    public User() {
        // Default constructor required by Spring Data/MongoDB
    }

    // All-Argument Constructor
    public User(String userId, String name, Long phone, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password; // NEW
    }

    // Constructor used during registration (no userId yet)
    public User(String name, Long phone, String email, String password) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password; // NEW
    }

    // =================================================================
    // 2. Getters and Setters (Updated to include password)
    // =================================================================

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPhone() {
        return phone;
    }

    public void setPhone(Long phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // NEW GETTER AND SETTER FOR PASSWORD
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
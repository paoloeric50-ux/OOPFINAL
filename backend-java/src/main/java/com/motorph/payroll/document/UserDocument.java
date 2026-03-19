package com.motorph.payroll.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * UserDocument.java - JPA Entity that maps to the "users" table
 *
 * Represents a system user (HR staff) who can log in and manage the payroll system.
 * Users are different from Employees — users can log in, employees are the ones
 * being paid.
 *
 * Security note: we never store plain-text passwords.
 * The password is hashed using BCrypt before being saved.
 *
 * Author: Group 5 - MO-IT101
 */
@Entity
@Table(name = "users")
public class UserDocument {

    // Primary key — UUID string
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    // Email is the login username — must be unique across all users
    @Column(name = "email", unique = true)
    private String email;

    // BCrypt hash of the password — never the plain password
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    // "ADMIN" or "USER" — column renamed to avoid SQL keyword conflict
    @Column(name = "role_value")
    private String role;

    // When the account was created (ISO-8601 string)
    @Column(name = "created_at")
    private String createdAt;

    // No-arg constructor required by JPA
    public UserDocument() {}

    // Standard getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Alias — getId() and getUserId() both return the same value
    public String getUserId() { return id; }
}

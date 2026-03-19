package com.motorph.payroll.service;

import com.motorph.payroll.document.UserDocument;
import com.motorph.payroll.dto.TokenResponseDto;
import com.motorph.payroll.dto.UserCreateDto;
import com.motorph.payroll.dto.UserLoginDto;
import com.motorph.payroll.dto.UserResponseDto;
import com.motorph.payroll.repository.UserRepository;
import com.motorph.payroll.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/*
 * AuthService.java - Handles user registration and login
 *
 * This service manages the two authentication endpoints:
 * - /api/auth/register : create a new user account
 * - /api/auth/login    : log in with email and password
 *
 * Security design:
 * - Passwords are NEVER stored as plain text
 * - We use BCryptPasswordEncoder to hash the password before saving
 * - BCrypt includes a random salt automatically, so two identical passwords
 *   will produce different hashes in the database
 * - When logging in, passwordEncoder.matches() compares the input against
 *   the stored hash without ever reversing the hash
 *
 * After successful login or register, a JWT token is returned.
 * The frontend stores this token and sends it in the Authorization header
 * for every subsequent API request.
 *
 * Author: Group 5 - MO-IT101
 */
@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    /*
     * register() - Create a new user account
     * 1. Check if the email is already registered
     * 2. Hash the password using BCrypt
     * 3. Save the new user to the database
     * 4. Generate a JWT token
     * 5. Return the token + user info
     */
    public TokenResponseDto register(UserCreateDto dto) {
        // reject duplicate emails
        Optional<UserDocument> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        // build the user record
        String userId = UUID.randomUUID().toString();
        UserDocument user = new UserDocument();
        user.setId(userId);
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));  // BCrypt hash
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setCreatedAt(Instant.now().toString());

        userRepository.save(user);

        // generate and return a JWT so the user is immediately logged in
        String token = jwtUtil.generateToken(userId, dto.getEmail(), dto.getRole());
        UserResponseDto userResponse = new UserResponseDto(userId, dto.getEmail(),
                dto.getFirstName(), dto.getLastName(), dto.getRole());
        return new TokenResponseDto(token, userResponse);
    }

    /*
     * login() - Authenticate an existing user
     * 1. Find the user by email (throw 401 if not found)
     * 2. Compare the provided password with the stored BCrypt hash
     * 3. If passwords match, generate and return a JWT
     *
     * Note: We return the same error message ("Invalid credentials") for both
     * "email not found" and "wrong password" — this prevents attackers from
     * figuring out which emails are registered in the system.
     */
    public TokenResponseDto login(UserLoginDto dto) {
        UserDocument user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // BCrypt.matches() hashes the input and compares — never decrypts the stored hash
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // return a new token + user profile info
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        UserResponseDto userResponse = new UserResponseDto(user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole());
        return new TokenResponseDto(token, userResponse);
    }

    // Return the current user's profile info (no password, of course)
    public UserResponseDto getMe(UserDocument user) {
        return new UserResponseDto(user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole());
    }
}

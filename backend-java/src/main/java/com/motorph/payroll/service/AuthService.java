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

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    public TokenResponseDto register(UserCreateDto dto) {
        Optional<UserDocument> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        String userId = UUID.randomUUID().toString();
        UserDocument user = new UserDocument();
        user.setId(userId);
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setCreatedAt(Instant.now().toString());
        userRepository.save(user);

        String token = jwtUtil.generateToken(userId, dto.getEmail(), dto.getRole());
        UserResponseDto userResponse = new UserResponseDto(userId, dto.getEmail(),
                dto.getFirstName(), dto.getLastName(), dto.getRole());
        return new TokenResponseDto(token, userResponse);
    }

    public TokenResponseDto login(UserLoginDto dto) {
        UserDocument user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        UserResponseDto userResponse = new UserResponseDto(user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole());
        return new TokenResponseDto(token, userResponse);
    }

    public UserResponseDto getMe(UserDocument user) {
        return new UserResponseDto(user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole());
    }
}

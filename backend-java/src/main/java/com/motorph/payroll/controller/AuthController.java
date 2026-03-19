package com.motorph.payroll.controller;

import com.motorph.payroll.document.UserDocument;
import com.motorph.payroll.dto.TokenResponseDto;
import com.motorph.payroll.dto.UserCreateDto;
import com.motorph.payroll.dto.UserLoginDto;
import com.motorph.payroll.dto.UserResponseDto;
import com.motorph.payroll.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/register")
    public TokenResponseDto register(@RequestBody UserCreateDto dto) {
        return authService.register(dto);
    }

    @PostMapping("/login")
    public TokenResponseDto login(@RequestBody UserLoginDto dto) {
        return authService.login(dto);
    }

    @GetMapping("/me")
    public UserResponseDto getMe(HttpServletRequest request) {
        UserDocument user = (UserDocument) request.getAttribute("currentUser");
        return authService.getMe(user);
    }
}

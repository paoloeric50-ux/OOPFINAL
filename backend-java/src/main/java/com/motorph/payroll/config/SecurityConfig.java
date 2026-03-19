package com.motorph.payroll.config;

import com.motorph.payroll.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * SecurityConfig.java - Configures Spring Security for the whole application
 *
 * This class does two main things:
 *  1. Defines which routes are public (no login needed) and which are protected
 *  2. Plugs in our JwtAuthFilter so every request gets checked for a valid token
 *
 * We use STATELESS sessions because JWTs carry user identity — there's no need
 * for the server to remember session data between requests.
 *
 * Author: Group 5 - MO-IT101
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    /*
     * filterChain() - The main security rule setup
     * Rules are applied in order — first match wins.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // disable CSRF because we use JWT (not cookies/sessions)
            .csrf(AbstractHttpConfigurer::disable)

            // use stateless sessions — no server-side session state
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // public pages anyone can visit
                .requestMatchers("/", "/login", "/register").permitAll()

                // main app pages — we handle auth in JavaScript on these
                .requestMatchers("/dashboard", "/employees/**", "/attendance", "/payroll", "/oop-concepts").permitAll()

                // static assets (CSS, JS, images) are always public
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // public API endpoints (login, register, deduction calculator)
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers("/api/deductions/calculate", "/api/deductions/tables").permitAll()
                .requestMatchers("/api/health", "/api/").permitAll()
                .requestMatchers("/api/oop/**").permitAll()

                // everything else requires a valid JWT
                .anyRequest().authenticated()
            )

            // add our JWT filter before the default login filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
     * passwordEncoder() - BCrypt password hashing
     * BCrypt automatically adds salt and hashes the password many times,
     * making it very hard to crack even if the database is stolen.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

package com.motorph.payroll.security;

import com.motorph.payroll.document.UserDocument;
import com.motorph.payroll.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/*
 * JwtAuthFilter.java - Runs before every HTTP request to check if the user is logged in
 *
 * This is a Spring Security filter. It intercepts every incoming request and checks
 * the "Authorization" header for a JWT token. If the token is valid, it marks the
 * user as authenticated so they can access protected routes.
 *
 * The header format is:  Authorization: Bearer <token>
 *
 * If no valid token is present, the request is still passed along but the user
 * won't be authenticated (so protected routes will reject them).
 *
 * Author: Group 5 - MO-IT101
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    /*
     * doFilterInternal() - Called for every incoming HTTP request
     * 1. Read the Authorization header
     * 2. Extract the token (remove the "Bearer " prefix)
     * 3. Validate it
     * 4. Look up the user in the database
     * 5. Register them as authenticated in the security context
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // only process if there's a Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);  // strip "Bearer " prefix

            try {
                if (jwtUtil.validateToken(token)) {
                    String userId = jwtUtil.extractUserId(token);
                    String role   = jwtUtil.extractRole(token);

                    // look up the user by ID to make sure they still exist in the DB
                    Optional<UserDocument> userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent()) {
                        UserDocument user = userOpt.get();

                        // store the user object in the request so controllers can access it
                        request.setAttribute("currentUser", user);

                        // tell Spring Security this request is authenticated
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                // if anything goes wrong, clear any partial authentication
                SecurityContextHolder.clearContext();
            }
        }

        // always pass the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}

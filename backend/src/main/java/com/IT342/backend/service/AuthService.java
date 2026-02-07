package com.IT342.backend.service;

import com.IT342.backend.model.AuthRequest;
import com.IT342.backend.model.AuthResponse;
import com.IT342.backend.model.User;
import com.IT342.backend.repository.UserRepository;
import com.IT342.backend.security.CustomUserDetailsService;
import com.IT342.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(User user) {
        // Check if user exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user
        User savedUser = userRepository.save(user);

        // Generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        final String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, savedUser.getUsername(), savedUser.getEmail());
    }

    public AuthResponse login(AuthRequest authRequest) {
        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        // Generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        final String token = jwtUtil.generateToken(userDetails);

        // Get user details
        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }
}
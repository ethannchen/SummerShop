package com.summershop.auth.service;

import com.summershop.auth.dto.*;
import com.summershop.auth.entity.User;
import com.summershop.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("accountId", user.getAccountId());

        String accessToken = jwtService.generateToken(user.getUsername(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .username(user.getUsername())
                .email(user.getEmail())
                .accountId(user.getAccountId())
                .build();
    }

    public TokenValidationResponse validateToken(TokenValidationRequest request) {
        log.info("Validating token");

        boolean isValid = jwtService.validateToken(request.getToken());

        if (!isValid) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }

        String username = jwtService.extractUsername(request.getToken());
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null || !user.getActive()) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }

        return TokenValidationResponse.builder()
                .valid(true)
                .username(user.getUsername())
                .email(user.getEmail())
                .accountId(user.getAccountId())
                .build();
    }

    @Transactional
    public LoginResponse refreshToken(TokenValidationRequest request) {
        log.info("Refreshing token");

        if (!jwtService.validateToken(request.getToken())) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(request.getToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getToken().equals(user.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("accountId", user.getAccountId());

        String newAccessToken = jwtService.generateToken(user.getUsername(), claims);
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .username(user.getUsername())
                .email(user.getEmail())
                .accountId(user.getAccountId())
                .build();
    }
}
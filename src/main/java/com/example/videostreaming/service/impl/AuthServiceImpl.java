package com.example.videostreaming.service.impl;

import com.example.videostreaming.dto.request.SignupRequest;
import com.example.videostreaming.dto.request.SigninRequest;
import com.example.videostreaming.dto.response.AuthResult;
import com.example.videostreaming.dto.response.JwtResponse;
import com.example.videostreaming.entity.RefreshToken;
import com.example.videostreaming.entity.User;
import com.example.videostreaming.repository.RefreshTokenRepository;
import com.example.videostreaming.repository.UserRepository;
import com.example.videostreaming.security.JwtUtils;
import com.example.videostreaming.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${jwt.refresh-expiration-ms:604800000}") // Default 7 days
    private long refreshExpirationMs;

    @Override
    @Transactional
    public void signup(SignupRequest request) {
        log.info("Processing signup request for username: '{}'", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("User registered successfully: '{}'", request.getUsername());
    }

    @Override
    @Transactional
    public AuthResult signin(SigninRequest request) {
        log.info("Processing signin request for: '{}'", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername());
        RefreshToken refreshToken = createRefreshToken(user);

        JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken(accessToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return new AuthResult(jwtResponse, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResult refresh(String refreshTokenString) {
        log.info("Processing token refresh request");

        if (refreshTokenString == null || refreshTokenString.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        RefreshToken oldToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (oldToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new IllegalArgumentException("Refresh token has expired. Please sign in again.");
        }

        User user = oldToken.getUser();

        // Rotate token: Delete the old one and generate a new one
        refreshTokenRepository.delete(oldToken);

        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getUsername());
        RefreshToken newRefreshToken = createRefreshToken(user);

        JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken(newAccessToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return new AuthResult(jwtResponse, newRefreshToken.getToken());
    }

    @Override
    @Transactional
    public void logout(String refreshTokenString) {
        log.info("Processing logout request");
        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenString)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}

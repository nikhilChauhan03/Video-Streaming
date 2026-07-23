package com.example.videostreaming.controller;

import com.example.videostreaming.dto.request.SignupRequest;
import com.example.videostreaming.dto.request.SigninRequest;
import com.example.videostreaming.dto.response.ApiResponse;
import com.example.videostreaming.dto.response.AuthResult;
import com.example.videostreaming.dto.response.JwtResponse;
import com.example.videostreaming.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Received request for signup: '{}'", request.getUsername());
        authService.signup(request);
        return ApiResponse.success("User registered successfully");
    }

    @PostMapping("/signin")
    public ApiResponse<JwtResponse> signin(@Valid @RequestBody SigninRequest request, HttpServletResponse response) {
        log.info("Received request for signin: '{}'", request.getUsername());
        AuthResult authResult = authService.signin(request);
        setRefreshTokenCookie(response, authResult.getRefreshToken());
        return ApiResponse.success("Signed in successfully", authResult.getJwtResponse());
    }

    @PostMapping("/refresh")
    public ApiResponse<JwtResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        log.info("Received request to refresh token");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cookie is missing");
        }
        AuthResult authResult = authService.refresh(refreshToken);
        setRefreshTokenCookie(response, authResult.getRefreshToken());
        return ApiResponse.success("Token refreshed successfully", authResult.getJwtResponse());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        log.info("Received request to logout");
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        clearRefreshTokenCookie(response);
        return ApiResponse.success("Logged out successfully");
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

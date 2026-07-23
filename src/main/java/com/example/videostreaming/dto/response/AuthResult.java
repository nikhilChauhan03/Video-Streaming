package com.example.videostreaming.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResult {
    private final JwtResponse jwtResponse;
    private final String refreshToken;
}

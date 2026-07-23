package com.example.videostreaming.service;

import com.example.videostreaming.dto.request.SignupRequest;
import com.example.videostreaming.dto.request.SigninRequest;
import com.example.videostreaming.dto.response.AuthResult;

public interface AuthService {
    void signup(SignupRequest request);
    AuthResult signin(SigninRequest request);
    AuthResult refresh(String refreshToken);
    void logout(String refreshToken);
}

package com.raydo.raydoApplication.service;

import com.raydo.raydoApplication.dto.AuthResponseDTO;

public interface AuthService {

    void requestOtp(String phoneNumber);

    AuthResponseDTO verifyOtp(String phoneNumber, String otp);
}
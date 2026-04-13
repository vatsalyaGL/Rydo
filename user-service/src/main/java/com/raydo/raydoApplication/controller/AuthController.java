package com.raydo.raydoApplication.controller;

import com.raydo.raydoApplication.dto.AuthResponseDTO;
import com.raydo.raydoApplication.dto.RequestOtpDTO;
import com.raydo.raydoApplication.dto.VerifyOtpDTO;
import com.raydo.raydoApplication.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody RequestOtpDTO request) {

        authService.requestOtp(request.getPhoneNumber());

        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDTO> verifyOtp(@RequestBody VerifyOtpDTO request) {

        AuthResponseDTO response = authService.verifyOtp(
                request.getPhoneNumber(),
                request.getOtp()
        );

        return ResponseEntity.ok(response);
    }
}
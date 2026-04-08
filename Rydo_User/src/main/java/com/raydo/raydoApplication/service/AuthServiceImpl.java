package com.raydo.raydoApplication.service;


import com.raydo.raydoApplication.config.TokenUtil;
import com.raydo.raydoApplication.dto.AuthResponseDTO;
import com.raydo.raydoApplication.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;

    @Override
    public void requestOtp(String phoneNumber) {

        String otp = generateOtp();

        otpStore.put(phoneNumber, new OtpData(
                otp,
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
        ));

        System.out.println("OTP for " + phoneNumber + " is: " + otp);
    }

    @Override
    public AuthResponseDTO verifyOtp(String phoneNumber, String otp) {

        OtpData data = otpStore.get(phoneNumber);

        if (data == null) {
            throw new RuntimeException("OTP not requested");
        }

        if (LocalDateTime.now().isAfter(data.expiry())) {
            otpStore.remove(phoneNumber);
            throw new RuntimeException("OTP expired");
        }

        if (!data.otp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        otpStore.remove(phoneNumber);

        UserResponseDTO user;
        boolean isNewUser = false;

        try {
            user = userService.getUserByPhone(phoneNumber);
        } catch (Exception e) {
            user = userService.createUser(phoneNumber, "New User");
            isNewUser = true;
        }

        String accessToken = TokenUtil.generateToken(user.getId().toString());
        String refreshToken = TokenUtil.generateToken(user.getId().toString());

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .isNewUser(isNewUser)
                .build();
    }

    private String generateOtp() {
        int otp = new Random().nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    private record OtpData(String otp, LocalDateTime expiry) {}
}
package com.raydo.raydoApplication.service;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.raydo.raydoApplication.config.TokenUtil;
import com.raydo.raydoApplication.dto.AuthResponseDTO;
import com.raydo.raydoApplication.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
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

    private void sendOtpFromPhone(String phoneNumber, String otp) {

        String url = "http://192.168.0.101:8080/message"; 
        String username = "sms";
        String password = "-WVC2yjn";

        RestTemplate restTemplate = new RestTemplate();

        // 🔐 Basic Auth
        String auth = username + ":" + password;
        String encodedAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", encodedAuth);

        HashMap<String, Object> textMessage = new HashMap<>();
        textMessage.put("text", "Your OTP is: " + otp);

        HashMap<String, Object> body = new HashMap<>();
        body.put("textMessage", textMessage);
        body.put("phoneNumbers", List.of(phoneNumber));

        HttpEntity<HashMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            System.out.println("SMS Response: " + response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("SMS sending failed", e);
        }
    }
    @Override
    public void requestOtp(String phoneNumber) {

        String otp = generateOtp();

        otpStore.put(phoneNumber, new OtpData(
                otp,
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
        ));
        sendOtpFromPhone(phoneNumber, otp);
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
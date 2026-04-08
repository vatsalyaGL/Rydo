package com.raydo.raydoApplication.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TokenUtil {

    private static final String SECRET = "1234567890";
    private static final long EXPIRY_TIME = 1000 * 60 * 60; // 1 hour

    public static String generateToken(String userId) {

        long expiry = System.currentTimeMillis() + EXPIRY_TIME;

        String data = userId + ":" + expiry;

        String signature = hmacSha256(data, SECRET);

        String token = data + ":" + signature;

        return Base64.getEncoder().encodeToString(token.getBytes());
    }

    public static String validateToken(String token) {

        try {
            String decoded = new String(Base64.getDecoder().decode(token));

            String[] parts = decoded.split(":");

            if (parts.length != 3) {
                throw new RuntimeException("Invalid token format");
            }

            String userId = parts[0];
            long expiry = Long.parseLong(parts[1]);
            String signature = parts[2];

            if (System.currentTimeMillis() > expiry) {
                throw new RuntimeException("Token expired");
            }

            String data = userId + ":" + expiry;
            String expectedSignature = hmacSha256(data, SECRET);

            if (!expectedSignature.equals(signature)) {
                throw new RuntimeException("Invalid token signature");
            }

            return userId;

        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }

    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(key);

            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(rawHmac);

        } catch (Exception e) {
            throw new RuntimeException("Error generating signature");
        }
    }
}
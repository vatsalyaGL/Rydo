package com.raydo.raydoApplication.config;

public class phoneUtil {
    public static String normalizePhoneNumber(String phone) {

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        phone = phone.replaceAll("\\s+", ""); // remove spaces

        if (phone.startsWith("+91")) {
            phone = phone.substring(3);
        }

        else if (phone.startsWith("91") && phone.length() == 12) {
            phone = phone.substring(2);
        }

        else if (phone.startsWith("0")) {
            phone = phone.substring(1);
        }

        if (!phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Invalid phone number: " + phone);
        }

        return "+91" + phone;
    }
}

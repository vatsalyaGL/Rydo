package com.raydo.raydoApplication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpDTO {
    private String phoneNumber;
    private String otp;
}
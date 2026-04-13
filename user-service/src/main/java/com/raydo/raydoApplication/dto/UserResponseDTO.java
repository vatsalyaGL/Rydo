package com.raydo.raydoApplication.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {

    private UUID id;
    private String fullName;
    private String phoneNumber;
    private String email;

    private String role;
    private Double ratingAvg;
}
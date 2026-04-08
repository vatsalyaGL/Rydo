package com.raydo.raydoApplication.config;


import com.raydo.raydoApplication.dto.UserResponseDTO;
import com.raydo.raydoApplication.entity.User;

public class UserMapper {

    public static UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole().name())
                .ratingAvg(user.getRatingAvg())
                .build();
    }
}
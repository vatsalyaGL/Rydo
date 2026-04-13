package com.raydo.raydoApplication.service;

import com.raydo.raydoApplication.dto.UserResponseDTO;
import com.raydo.raydoApplication.dto.UserUpdateDTO;
import com.raydo.raydoApplication.entity.Role;

import java.util.UUID;

public interface UserService {


        UserResponseDTO getUserById(UUID userId);

        UserResponseDTO getUserByPhone(String phoneNumber);

        UserResponseDTO createUser(String phoneNumber, String fullName);

        UserResponseDTO updateUser(UUID userId, UserUpdateDTO dto);

        void deleteUser(UUID userId);


}

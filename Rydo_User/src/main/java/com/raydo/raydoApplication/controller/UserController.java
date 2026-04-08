package com.raydo.raydoApplication.controller;

import com.raydo.raydoApplication.config.phoneUtil;
import com.raydo.raydoApplication.dto.UserCreateDTO;
import com.raydo.raydoApplication.dto.UserResponseDTO;
import com.raydo.raydoApplication.dto.UserUpdateDTO;
import com.raydo.raydoApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {

        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/phone")
    public ResponseEntity<UserResponseDTO> getUserByPhone(
            @RequestParam String phoneNumber) {
        String phone = phoneUtil.normalizePhoneNumber(phoneNumber);

        UserResponseDTO user = userService.getUserByPhone(phone);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @RequestBody UserCreateDTO request) {

        UserResponseDTO user = userService.createUser(
                request.getPhoneNumber(),
                request.getFullName()
        );

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @RequestBody UserUpdateDTO request) {

        UserResponseDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {

        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }


}
package com.raydo.raydoApplication.service;

import com.raydo.raydoApplication.Exceptions.DuplicateUserException;
import com.raydo.raydoApplication.Exceptions.UserNotFoundException;
import com.raydo.raydoApplication.config.UserMapper;
import com.raydo.raydoApplication.dto.UserResponseDTO;
import com.raydo.raydoApplication.dto.UserUpdateDTO;
import com.raydo.raydoApplication.entity.Role;
import com.raydo.raydoApplication.entity.Status;
import com.raydo.raydoApplication.entity.User;
import com.raydo.raydoApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDTO getUserById(UUID userId) {

        log.info("Fetching user by id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return UserMapper.toDTO(user);
    }

    @Override
    public UserResponseDTO getUserByPhone(String phoneNumber) {

        log.info("Fetching user by phone: {}", phoneNumber);

        String normalizedPhone = normalizePhone(phoneNumber);

        User user = userRepository.findByPhoneNumber(normalizedPhone)
                .orElseThrow(() -> new UserNotFoundException("User not found with phone: " + normalizedPhone));

        return UserMapper.toDTO(user);
    }

    @Override
    public UserResponseDTO createUser(String phoneNumber, String fullName) {

        log.info("Creating user with phone: {}", phoneNumber);

        String normalizedPhone = normalizePhone(phoneNumber);

        if (userRepository.existsByPhoneNumber(normalizedPhone)) {
            log.warn("User already exists with phone: {}", normalizedPhone);
            throw new DuplicateUserException("User already exists with phone number: " + normalizedPhone);
        }

        User user = User.builder()
                .phoneNumber(normalizedPhone)
                .fullName(fullName)
                .fullName(fullName)
                .role(Role.RIDER)
                .status(Status.ACTIVE)
                .ratingAvg(5.0)
                .ratingCount(0)
                .preferredLanguage("en-US")
                .build();

        user = userRepository.save(user);

        log.info("User created successfully with id: {}", user.getId());

        return UserMapper.toDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(UUID userId, UserUpdateDTO dto) {

        log.info("Updating user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {

            if (userRepository.existsByEmail(dto.getEmail())
                    && (user.getEmail() == null || !user.getEmail().equals(dto.getEmail()))) {

                log.warn("Duplicate email attempt: {}", dto.getEmail());
                throw new DuplicateUserException("Email already in use: " + dto.getEmail());
            }

            user.setEmail(dto.getEmail());
        }

        user = userRepository.save(user);

        log.info("User updated successfully: {}", userId);

        return UserMapper.toDTO(user);
    }

    @Override
    public void deleteUser(UUID userId) {

        log.info("Deleting user (soft delete) with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setStatus(Status.DISABLED);

        userRepository.save(user);

        log.info("User soft-deleted successfully: {}", userId);
    }

    public String normalizePhone(String phone) {

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        phone = phone.trim();

        phone = phone.replaceAll("[^0-9]", "");

        if (phone.startsWith("91") && phone.length() == 12) {
            return "+" + phone;
        }

        if (phone.length() == 10) {
            return "+91" + phone;
        }

        if (phone.startsWith("+91")) {
            return phone;
        }

        throw new IllegalArgumentException("Invalid phone number: " + phone);
    }

}
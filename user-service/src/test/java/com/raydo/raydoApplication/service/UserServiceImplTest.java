package com.raydo.raydoApplication.service;

import com.raydo.raydoApplication.Exceptions.DuplicateUserException;
import com.raydo.raydoApplication.Exceptions.UserNotFoundException;
import com.raydo.raydoApplication.dto.UserResponseDTO;
import com.raydo.raydoApplication.dto.UserUpdateDTO;
import com.raydo.raydoApplication.entity.Role;
import com.raydo.raydoApplication.entity.Status;
import com.raydo.raydoApplication.entity.User;
import com.raydo.raydoApplication.repository.UserRepository;
import com.raydo.raydoApplication.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = User.builder()
                .id(userId)
                .fullName("John Doe")
                .phoneNumber("+919876543210")
                .email("john@example.com")
                .role(Role.RIDER)
                .status(Status.ACTIVE)
                .ratingAvg(5.0)
                .ratingCount(0)
                .preferredLanguage("en-US")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("returns DTO when user exists")
        void returnsDTO_whenUserExists() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

            UserResponseDTO result = userService.getUserById(userId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getFullName()).isEqualTo("John Doe");
            assertThat(result.getPhoneNumber()).isEqualTo("+919876543210");
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsException_whenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(userId.toString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserByPhone
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUserByPhone()")
    class GetUserByPhone {

        @Test
        @DisplayName("normalises 10-digit number and returns DTO")
        void normalises_andReturnsDTO() {
            when(userRepository.findByPhoneNumber("+919876543210"))
                    .thenReturn(Optional.of(sampleUser));

            UserResponseDTO result = userService.getUserByPhone("9876543210");

            assertThat(result.getPhoneNumber()).isEqualTo("+919876543210");
        }

        @Test
        @DisplayName("throws UserNotFoundException for unknown phone")
        void throwsException_whenPhoneNotFound() {
            when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByPhone("9876543210"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("creates and returns user successfully")
        void createsUser_successfully() {
            when(userRepository.existsByPhoneNumber("+919876543210")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);

            UserResponseDTO result = userService.createUser("9876543210", "John Doe");

            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo("John Doe");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws DuplicateUserException when phone already exists")
        void throwsDuplicateException_whenPhoneExists() {
            when(userRepository.existsByPhoneNumber("+919876543210")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser("9876543210", "John Doe"))
                    .isInstanceOf(DuplicateUserException.class)
                    .hasMessageContaining("+919876543210");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("sets default RIDER role and ACTIVE status")
        void setsDefaultRoleAndStatus() {
            when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // We verify by checking what gets saved
            userService.createUser("9876543210", "Jane");

            verify(userRepository).save(argThat(u ->
                    u.getRole() == Role.RIDER &&
                            u.getStatus() == Status.ACTIVE &&
                            u.getRatingAvg() == 5.0
            ));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        @Test
        @DisplayName("updates fullName and email when both provided")
        void updatesNameAndEmail() {
            UserUpdateDTO dto = new UserUpdateDTO();
            dto.setFullName("Jane Updated");
            dto.setEmail("jane@example.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponseDTO result = userService.updateUser(userId, dto);

            assertThat(result.getFullName()).isEqualTo("Jane Updated");
        }

        @Test
        @DisplayName("throws UserNotFoundException when user missing")
        void throwsException_whenUserMissing() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(userId, new UserUpdateDTO()))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("throws DuplicateUserException when email taken by another user")
        void throwsDuplicate_whenEmailTaken() {
            UserUpdateDTO dto = new UserUpdateDTO();
            dto.setEmail("taken@example.com");

            sampleUser.setEmail("current@example.com"); // user has a different current email
            when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(userId, dto))
                    .isInstanceOf(DuplicateUserException.class)
                    .hasMessageContaining("taken@example.com");
        }

        @Test
        @DisplayName("does not throw DuplicateException when email belongs to same user")
        void doesNotThrow_whenEmailBelongsToSameUser() {
            UserUpdateDTO dto = new UserUpdateDTO();
            dto.setEmail("john@example.com"); // same as sampleUser.email

            when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() -> userService.updateUser(userId, dto)).doesNotThrowAnyException();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("soft-deletes user by setting status to DISABLED")
        void softDeletesUser() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.deleteUser(userId);

            assertThat(sampleUser.getStatus()).isEqualTo(Status.DISABLED);
            verify(userRepository).save(sampleUser);
        }

        @Test
        @DisplayName("throws UserNotFoundException when user not found")
        void throwsException_whenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // normalizePhone
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("normalizePhone()")
    class NormalizePhone {

        @Test
        @DisplayName("normalises 10-digit number to +91 format")
        void normalises10Digit() {
            assertThat(userService.normalizePhone("9876543210")).isEqualTo("+919876543210");
        }

        @Test
        @DisplayName("normalises 12-digit number starting with 91")
        void normalises12Digit() {
            assertThat(userService.normalizePhone("919876543210")).isEqualTo("+919876543210");
        }

        @Test
        @DisplayName("keeps already normalised +91 number unchanged")
        void keepsNormalised() {
            assertThat(userService.normalizePhone("+919876543210")).isEqualTo("+919876543210");
        }

        @Test
        @DisplayName("strips non-digit characters before normalising")
        void stripsNonDigits() {
            assertThat(userService.normalizePhone("+91-9876-543210")).isEqualTo("+919876543210");
        }

        @Test
        @DisplayName("throws IllegalArgumentException for null input")
        void throwsForNull() {
            assertThatThrownBy(() -> userService.normalizePhone(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws IllegalArgumentException for blank input")
        void throwsForBlank() {
            assertThatThrownBy(() -> userService.normalizePhone("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws IllegalArgumentException for invalid length")
        void throwsForInvalidLength() {
            assertThatThrownBy(() -> userService.normalizePhone("123"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
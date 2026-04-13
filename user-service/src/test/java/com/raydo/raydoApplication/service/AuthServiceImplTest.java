package com.raydo.raydoApplication.service;

import com.raydo.raydoApplication.dto.AuthResponseDTO;
import com.raydo.raydoApplication.dto.UserResponseDTO;
import com.raydo.raydoApplication.Exceptions.UserNotFoundException;
import com.raydo.raydoApplication.service.AuthServiceImpl;
import com.raydo.raydoApplication.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private final String PHONE = "+919876543210";
    private UserResponseDTO existingUser;

    @BeforeEach
    void setUp() {
        existingUser = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .fullName("Test User")
                .phoneNumber(PHONE)
                .role("RIDER")
                .ratingAvg(5.0)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // requestOtp
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("requestOtp()")
    class RequestOtp {

        @Test
        @DisplayName("stores OTP without throwing")
        void storesOtpSuccessfully() {
            assertThatCode(() -> authService.requestOtp(PHONE))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("overwrites previous OTP when called twice")
        void overwritesPreviousOtp() {
            authService.requestOtp(PHONE);
            // second call should not throw
            assertThatCode(() -> authService.requestOtp(PHONE))
                    .doesNotThrowAnyException();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // verifyOtp
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("verifyOtp()")
    class VerifyOtp {

        @Test
        @DisplayName("returns AuthResponseDTO with tokens for existing user")
        void returnsAuthResponse_forExistingUser() {

            AuthServiceImpl spy = Mockito.spy(authService);

            injectKnownOtp(authService, PHONE, "123456");
            when(userService.getUserByPhone(PHONE)).thenReturn(existingUser);

            AuthResponseDTO result = authService.verifyOtp(PHONE, "123456");

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isNotBlank();
            assertThat(result.getRefreshToken()).isNotBlank();
            assertThat(result.isNewUser()).isFalse();
            assertThat(result.getUser().getPhoneNumber()).isEqualTo(PHONE);
        }

        @Test
        @DisplayName("creates new user when phone not yet registered")
        void createsNewUser_whenPhoneUnregistered() {
            injectKnownOtp(authService, PHONE, "654321");
            when(userService.getUserByPhone(PHONE))
                    .thenThrow(new UserNotFoundException("not found"));
            when(userService.createUser(PHONE, "New User")).thenReturn(existingUser);

            AuthResponseDTO result = authService.verifyOtp(PHONE, "654321");

            assertThat(result.isNewUser()).isTrue();
            verify(userService).createUser(PHONE, "New User");
        }

        @Test
        @DisplayName("throws RuntimeException when OTP not requested")
        void throwsWhenOtpNotRequested() {
            assertThatThrownBy(() -> authService.verifyOtp(PHONE, "000000"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("OTP not requested");
        }

        @Test
        @DisplayName("throws RuntimeException for wrong OTP")
        void throwsForWrongOtp() {
            injectKnownOtp(authService, PHONE, "111111");

            assertThatThrownBy(() -> authService.verifyOtp(PHONE, "999999"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid OTP");
        }

        @Test
        @DisplayName("throws RuntimeException when OTP is expired")
        void throwsForExpiredOtp() throws Exception {
            // Inject OTP with expiry in the past
            injectExpiredOtp(authService, PHONE, "777777");

            assertThatThrownBy(() -> authService.verifyOtp(PHONE, "777777"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("OTP expired");
        }

        @Test
        @DisplayName("clears OTP from store after successful verification")
        void clearsOtpAfterSuccess() {
            injectKnownOtp(authService, PHONE, "123456");
            when(userService.getUserByPhone(PHONE)).thenReturn(existingUser);

            authService.verifyOtp(PHONE, "123456");

            // Second verification attempt should fail – OTP already consumed
            assertThatThrownBy(() -> authService.verifyOtp(PHONE, "123456"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("OTP not requested");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Injects a known, non-expired OTP into the authService via reflection. */
    private void injectKnownOtp(AuthServiceImpl service, String phone, String otp) {
        try {
            var storeField = AuthServiceImpl.class.getDeclaredField("otpStore");
            storeField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> store =
                    (java.util.Map<String, Object>) storeField.get(service);

            Class<?> otpDataClass = null;
            for (Class<?> inner : AuthServiceImpl.class.getDeclaredClasses()) {
                if (inner.getSimpleName().equals("OtpData")) {
                    otpDataClass = inner;
                    break;
                }
            }
            assert otpDataClass != null;
            var ctor = otpDataClass.getDeclaredConstructors()[0];
            ctor.setAccessible(true);
            Object otpData = ctor.newInstance(otp, java.time.LocalDateTime.now().plusMinutes(5));
            store.put(phone, otpData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject OTP via reflection", e);
        }
    }

    /** Injects an expired OTP into the authService via reflection. */
    private void injectExpiredOtp(AuthServiceImpl service, String phone, String otp) {
        try {
            var storeField = AuthServiceImpl.class.getDeclaredField("otpStore");
            storeField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> store =
                    (java.util.Map<String, Object>) storeField.get(service);

            Class<?> otpDataClass = null;
            for (Class<?> inner : AuthServiceImpl.class.getDeclaredClasses()) {
                if (inner.getSimpleName().equals("OtpData")) {
                    otpDataClass = inner;
                    break;
                }
            }
            assert otpDataClass != null;
            var ctor = otpDataClass.getDeclaredConstructors()[0];
            ctor.setAccessible(true);
            Object otpData = ctor.newInstance(otp, java.time.LocalDateTime.now().minusMinutes(1));
            store.put(phone, otpData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject expired OTP via reflection", e);
        }
    }
}
package com.rentalcar.server.restcontroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalcar.server.component.TokenGenerator;
import com.rentalcar.server.entity.ResetToken;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.*;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.repository.ResetTokenRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.service.AuthService;
import com.rentalcar.server.util.DateTimeUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository repository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthService authService;

    @Autowired
    ResetTokenRepository resetTokenRepository;

    @Autowired
    TokenGenerator tokenGenerator;

    @Autowired
    DateTimeUtils dateTimeUtils;

    private User userData;

    @BeforeEach
    void setUp() {
        var user = User.builder()
                .email("rafli@gmail.com")
                .phoneNumber("+6281232720821")
                .password(passwordEncoder.encode("secretpassword"))
                .name("rafli andreansyah")
                .role(UserRoleEnum.USER)
                .build();
        userData = repository.save(user);
    }

    @AfterEach
    void afterTest() {
        repository.deleteAll();
    }

    @Test
    void registerSuccessTest() throws Exception {
        repository.deleteById(userData.getId());
        var request = RegisterRequest
                .builder()
                .email("rafli@gmail.com")
                .phone("+6281232720821")
                .password("secretpassword")
                .name("rafli andreansyah")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isCreated()
        ).andExpectAll(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            Assertions.assertEquals("registration successful", response.getData());
        });

    }

    @Test
    void registerEmailNotValidTest() throws Exception {
        var request = RegisterRequest
                .builder()
                .email("rafliandrean")
                .phone("+6281232720821")
                .password("secretpassword")
                .name("rafli andreansyah")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("format email is not valid", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerPhoneNumberNotValidTest() throws Exception {
        var request = RegisterRequest
                .builder()
                .email("rafli@gmail.com")
                .phone("08232720821")
                .password("secretpassword")
                .name("rafli andreansyah")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("phone number is not valid", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerPasswordErrorTest() throws Exception {
        var request = RegisterRequest
                .builder()
                .email("rafli@gmail.com")
                .phone("+628232720821")
                .password("secret")
                .name("rafli andreansyah")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("password must have 8 characters or more", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerPasswordIsBlankErrorTest() throws Exception {
        var request = RegisterRequest
                .builder()
                .email("rafli@gmail.com")
                .phone("+628232720821")
                .name("rafli andreansyah")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("password must not be blank", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerNameIsBlankErrorTest() throws Exception {
        var request = RegisterRequest
                .builder()
                .email("rafli@gmail.com")
                .phone("+628232720821")
                .password("secretpassword")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("name must not be blank", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerPhoneIsBlankErrorTest() throws Exception {
        var request = RegisterRequest
                .builder()
                .email("rafli@gmail.com")
                .password("secretpassword")
                .name("rafli andreansyah")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("phone must not be blank", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerEmailIsBlankErrorTest() throws Exception {
        var request = RegisterRequest
                .builder()
                .phone("+628232720821")
                .password("secretpassword")
                .name("rafli andreansyah")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("email must not be blank", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void authenticateSuccessTest() throws Exception {

        var authRequest = AuthenticateRequest
                .builder()
                .email("rafli@gmail.com")
                .password("secretpassword")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(authRequest))
        ).andExpectAll(
                status().isOk()
        ).andExpectAll(result -> {
            var response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<AuthenticateResponse>>() {
            });
            Assertions.assertNull(response.getError());
            Assertions.assertNotNull(response.getData());
            Assertions.assertNotNull(response.getData().getToken());
            System.out.println(response.getData().getToken());
        });

    }

    @Test
    void authenticateEmailBlankErrorTest() throws Exception {

        var authRequest = AuthenticateRequest
                .builder()
                .password("secretpassword")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(authRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            var response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<AuthenticateResponse>>() {
            });
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            Assertions.assertEquals("email must be not blank", response.getError());
        });

    }

    @Test
    void authenticateFormatEmailErrorTest() throws Exception {

        var authRequest = AuthenticateRequest
                .builder()
                .email("rafliandrean")
                .password("secretpassword")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(authRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            var response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<AuthenticateResponse>>() {
            });
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            Assertions.assertEquals("format email is not valid", response.getError());
        });

    }

    @Test
    void authenticatePasswordBlankErrorTest() throws Exception {

        var authRequest = AuthenticateRequest
                .builder()
                .email("rafli@gmail.com")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(authRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            var response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<AuthenticateResponse>>() {
            });
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            Assertions.assertEquals("password must not be blank", response.getError());
        });

    }

    @Test
    void authenticateWrongEmailTest() throws Exception {

        var authRequest = AuthenticateRequest
                .builder()
                .email("azha@gmail.com")
                .password("secretpassword")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(authRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            var response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<AuthenticateResponse>>() {
            });
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            Assertions.assertEquals("invalid email or password", response.getError());
        });

    }

    @Test
    void authenticateWrongPasswordTest() throws Exception {

        var authRequest = AuthenticateRequest
                .builder()
                .email("rafli@gmail.com")
                .password("secretssword")
                .build();

        mockMvc.perform(
                post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(authRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andExpectAll(result -> {
            var response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<AuthenticateResponse>>() {
            });
            Assertions.assertNotNull(response.getError());
            Assertions.assertNull(response.getData());
            Assertions.assertEquals("invalid email or password", response.getError());
        });

    }

    @Test
    void requestResetPasswordUserSuccessTest() throws Exception {

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder().email(userData.getEmail()).build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/auth/reset-password")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsBytes(resetPasswordRequest))

                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals("success send link reset password to email", response.getData());

                });
    }

    @Test
    void requestResetPasswordUserNotFoundErrorTest() throws Exception {

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder().email("testing@gmail.com").build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/auth/reset-password")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsBytes(resetPasswordRequest))

                )
                .andExpectAll(status().isNotFound())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
                    Assertions.assertEquals("user not found", response.getError());

                });
    }

    @Test
    void getResetPasswordUserSuccessTest() throws Exception {
        Optional<ResetToken> resetTokenData = resetTokenRepository.findByUserId(userData.getId());
        resetTokenData.ifPresent(resetToken -> resetTokenRepository.deleteById(resetToken.getId()));

        // Generate token
        String token = tokenGenerator.generateToken();
        LocalDateTime localDateTime = LocalDateTime.now().plusHours(1);

        // Save data to reset token
        ResetToken resetTokenSaved = resetTokenRepository.save(ResetToken.builder()
                .token(token)
                .user(userData)
                .expiredDate(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(localDateTime))
                .build());


        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/auth/reset-password")
                                .param("token", resetTokenSaved.getToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<ResetPasswordResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(userData.getId().toString(), response.getData().getUserId());
                });
    }

    @Test
    void getResetPasswordTokenExpiredErrorTest() throws Exception {
        Optional<ResetToken> resetTokenData = resetTokenRepository.findByUserId(userData.getId());
        resetTokenData.ifPresent(resetToken -> resetTokenRepository.deleteById(resetToken.getId()));

        // Generate token
        String token = tokenGenerator.generateToken();
        LocalDateTime localDateTime = LocalDateTime.now().plusHours(1);

        // Save data to reset token
        ResetToken resetTokenSaved = resetTokenRepository.save(ResetToken.builder()
                .token(token)
                .user(userData)
                .expiredDate(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(localDateTime))
                .build());


        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/auth/reset-password")
                                .param("token", resetTokenSaved.getToken() + "dawd")
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpectAll(status().isGone())
                .andExpectAll(result -> {
                    WebResponse<ResetPasswordResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.GONE.value(), response.getStatus());
                    Assertions.assertEquals("link reset password is expired", response.getError());
                });
    }

    @Test
    void getResetPasswordTokenExpiredInvalidTokenErrorTest() throws Exception {
        Optional<ResetToken> resetTokenData = resetTokenRepository.findByUserId(userData.getId());
        resetTokenData.ifPresent(resetToken -> resetTokenRepository.deleteById(resetToken.getId()));

        // Generate token
        String token = tokenGenerator.generateToken();
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(1);

        // Save data to reset token
        ResetToken resetTokenSaved = resetTokenRepository.save(ResetToken.builder()
                .token(token)
                .user(userData)
                .expiredDate(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(localDateTime))
                .build());


        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/auth/reset-password")
                                .param("token", resetTokenSaved.getToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpectAll(status().isGone())
                .andExpectAll(result -> {
                    WebResponse<ResetPasswordResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.GONE.value(), response.getStatus());
                    Assertions.assertEquals("link reset password is expired", response.getError());
                });
    }

    @Test
    void resetNewPasswordSuccessTest() throws Exception {

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .email(userData.getEmail())
                .build();

        ResetToken resetToken = authService.requestResetPassword(resetPasswordRequest);

        ResetNewPasswordRequest resetNewPasswordRequest = ResetNewPasswordRequest.builder()
                .newPassword("testing")
                .token(resetToken.getToken())
                .userId(resetToken.getUser().getId().toString())
                .build();

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/auth/reset-new-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetNewPasswordRequest))
        )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                   WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                   });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals("success reset new password", response.getData());
                });

    }

    @Test
    void resetNewPasswordExpiredWrongTokenErrorTest() throws Exception {

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .email(userData.getEmail())
                .build();

        ResetToken resetToken = authService.requestResetPassword(resetPasswordRequest);

        ResetNewPasswordRequest resetNewPasswordRequest = ResetNewPasswordRequest.builder()
                .newPassword("testing")
                .token(resetToken.getToken() + "test")
                .userId(resetToken.getUser().getId().toString())
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/auth/reset-new-password")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetNewPasswordRequest))
                )
                .andExpectAll(status().isGone())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.GONE.value(), response.getStatus());
                    Assertions.assertEquals("reset password is expired", response.getError());
                });

    }

    @Test
    void resetNewPasswordExpiredWrongUserIDErrorTest() throws Exception {

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .email(userData.getEmail())
                .build();

        ResetToken resetToken = authService.requestResetPassword(resetPasswordRequest);

        ResetNewPasswordRequest resetNewPasswordRequest = ResetNewPasswordRequest.builder()
                .newPassword("testing")
                .token(resetToken.getToken())
                .userId("a80d02d6-56da-4264-a5fb-2ad1ef03f77d")
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/auth/reset-new-password")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetNewPasswordRequest))
                )
                .andExpectAll(status().isGone())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.GONE.value(), response.getStatus());
                    Assertions.assertEquals("reset password is expired", response.getError());
                });

    }
}
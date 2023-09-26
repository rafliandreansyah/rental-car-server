package com.rentalcar.server.restcontroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.AuthenticateRequest;
import com.rentalcar.server.model.AuthenticateResponse;
import com.rentalcar.server.model.RegisterRequest;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

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


    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void registerSuccessTest() throws Exception {

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
        var user = User.builder()
                .email("rafli@gmail.com")
                .phoneNumber("+6281232720821")
                .password(passwordEncoder.encode("secretpassword"))
                .name("rafli andreansyah")
                .role(UserRoleEnum.ADMIN)
                .build();
        repository.save(user);

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
        var user = User.builder()
                .email("rafli@gmail.com")
                .phoneNumber("+6281232720821")
                .password(passwordEncoder.encode("secretpassword"))
                .name("rafli andreansyah")
                .role(UserRoleEnum.ADMIN)
                .build();
        repository.save(user);

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
        var user = User.builder()
                .email("rafli@gmail.com")
                .phoneNumber("+6281232720821")
                .password(passwordEncoder.encode("secretpassword"))
                .name("rafli andreansyah")
                .role(UserRoleEnum.ADMIN)
                .build();
        repository.save(user);

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

}
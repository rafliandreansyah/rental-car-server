package com.rentalcar.server.restcontroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalcar.server.entity.*;
import com.rentalcar.server.model.*;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.model.base.WebResponsePaging;
import com.rentalcar.server.repository.TransactionRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.security.JwtService;
import com.rentalcar.server.service.AuthService;
import com.rentalcar.server.util.DateTimeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthService authService;

    @Autowired
    JwtService jwtService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;
    private User admin;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    DateTimeUtils dateTimeUtils;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();
        admin = authService.createAdmin(
                User.builder()
                        .name("Admin")
                        .email("admin@yahoo.com")
                        .password("amaterasu")
                        .phoneNumber("+628928383744")
                        .build()
        );
    }

    @Test
    void createUserSuccessWithImageTest() throws Exception {

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "rafli.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .password("secretpassword")
                .name("Jono Joni")
                .phone("+628999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file(mockMultipartFile)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isCreated())
                .andExpectAll(result -> {
                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getData().getId());
                    Assertions.assertNotNull(response.getData().getImageUrl());
                    Assertions.assertEquals(request.getEmail(), response.getData().getEmail());
                    Assertions.assertEquals(request.getName(), response.getData().getName());
                    Assertions.assertEquals(request.getPhone(), response.getData().getPhone());
                    Assertions.assertEquals(request.getRole().toLowerCase(), response.getData().getRole());
                    Assertions.assertEquals(dateTimeUtils.localDateFromInstantZoneJakarta(request.getDob()).toString(), response.getData().getDob());
                });
    }

    @Test
    void createUserSuccessWithOutImageTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .password("secretpassword")
                .name("Jono Joni")
                .phone("+628999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isCreated())
                .andExpectAll(result -> {
                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getData().getId());
                    Assertions.assertNull(response.getData().getImageUrl());
                    Assertions.assertEquals(request.getEmail(), response.getData().getEmail());
                    Assertions.assertEquals(request.getName(), response.getData().getName());
                    Assertions.assertEquals(request.getPhone(), response.getData().getPhone());
                    Assertions.assertEquals(request.getRole().toLowerCase(), response.getData().getRole());
                    Assertions.assertEquals(dateTimeUtils.localDateFromInstantZoneJakarta(request.getDob()).toString(), response.getData().getDob());
                });
    }

    @Test
    void createUserErrorEmailIsBlankTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .password("secretpassword")
                .name("Jono Joni")
                .phone("+628999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("email must not be blank", response.getError());
                });
    }

    @Test
    void createUserErrorFormatEmailNotValidTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono")
                .password("secretpassword")
                .name("Jono Joni")
                .phone("+628999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {

                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("format email is not valid", response.getError());

                });
    }

    @Test
    void createUserErrorNameIsBlankTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .password("secretpassword")
                .phone("+628999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("name must not be blank", response.getError());

                });
    }

    @Test
    void createUserErrorPasswordIsBlankTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .name("Jono Joni")
                .phone("+628999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("password must not be blank", response.getError());

                });
    }

    @Test
    void createUserErrorPasswordIsShortTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .password("secret")
                .name("Jono Joni")
                .phone("+628999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {

                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("password must have 8 characters or more", response.getError());

                });
    }

    @Test
    void createUserErrorPhoneNumberIsBlankTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .password("secretpassword")
                .name("Jono Joni")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {

                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("phone must not be blank", response.getError());


                });
    }

    @Test
    void createUserErrorFormatPhoneNumberIsNotValidTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .password("secretpassword")
                .name("Jono Joni")
                .phone("08999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("phone number is not valid", response.getError());
                });
    }

    @Test
    void createUserErrorUserRoleNotFoundTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .password("secretpassword")
                .name("Jono Joni")
                .phone("+628999366363")
                .dob(Instant.now())
                .role("super")
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("user role is not found", response.getError());

                });
    }

    @Test
    void createUserErrorEmailAlreadyRegisterTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("admin@yahoo.com")
                .password("secretpassword")
                .name("Jono Joni")
                .phone("+628999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isConflict())
                .andExpectAll(result -> {

                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("email already register", response.getError());

                });
    }

    @Test
    void createUserErrorPhoneNumberAlreadyUseTest() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .password("secretpassword")
                .name("Jono Joni")
                .phone("+628928383744")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isConflict())
                .andExpectAll(result -> {

                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("phone number already use", response.getError());

                });
    }

    @Test
    void createUserErrorForbiddenAccessTest() throws Exception {

        User user = User.builder()
                .name("Admin")
                .email("admin2@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+62892838399")
                .role(UserRoleEnum.USER)
                .build();
        User userSave = userRepository.save(user);

        CreateUserRequest request = CreateUserRequest.builder()
                .email("jono@gmail.com")
                .password("secretpassword")
                .name("Jono Joni")
                .phone("+628999366363")
                .dob(Instant.now())
                .role(UserRoleEnum.ADMIN.name())
                .build();

        String token = jwtService.generateToken(userSave);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/users")
                                .file("image", null)
                                .param("email", request.getEmail())
                                .param("password", request.getPassword())
                                .param("name", request.getName())
                                .param("phone", request.getPhone())
                                .param("role", request.getRole())
                                .param("dob", LocalDateTime.ofInstant(request.getDob(), ZoneId.of("Asia/Jakarta")).toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponse<CreateUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals("don't have a access", response.getError());

                });
    }

    @Test
    void getDetailUserSuccessTest() throws Exception {

        User user = User.builder()
                .name("User")
                .email("user@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+62892838399")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.USER)
                .build();
        User userSave = userRepository.save(user);

        String token = jwtService.generateToken(userSave);

        mockMvc.perform(
                        get("/api/v1/users/" + userSave.getId())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {

                    WebResponse<DetailUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());

                    Assertions.assertEquals(userSave.getId().toString(), response.getData().getId());
                    Assertions.assertEquals(userSave.getName(), response.getData().getName());
                    Assertions.assertEquals(userSave.getEmail(), response.getData().getEmail());
                    Assertions.assertEquals(userSave.getImageUrl(), response.getData().getImageUrl());
                    Assertions.assertEquals(dateTimeUtils.localDateFromInstantZoneJakarta(userSave.getDateOfBirth()).toString(), response.getData().getDob());
                    Assertions.assertEquals(userSave.getPhoneNumber(), response.getData().getPhone());
                    Assertions.assertEquals(userSave.getIsActive(), response.getData().isActive());
                    Assertions.assertNull(response.getData().getRole());
                    Assertions.assertEquals(LocalDateTime.ofInstant(userSave.getCreatedAt(), ZoneId.of("Asia/Jakarta")).toString(), response.getData().getDateCreated());

                });

    }

    @Test
    void getDetailUserErrorForbiddenTest() throws Exception {

        User user = User.builder()
                .name("User")
                .email("user@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+62892838399")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.USER)
                .build();

        User user2 = User.builder()
                .name("User")
                .email("user2@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+62892858399")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.USER)
                .build();
        User userSave = userRepository.save(user);
        User userSave2 = userRepository.save(user2);

        String token = jwtService.generateToken(userSave);

        mockMvc.perform(
                        get("/api/v1/users/" + userSave2.getId())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isForbidden())
                .andExpectAll(result -> {

                    WebResponse<DetailUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

                    Assertions.assertEquals("don't have a access", response.getError());

                });

    }

    @Test
    void getDetailUserByAdminSuccessTest() throws Exception {

        User user = User.builder()
                .name("User")
                .email("user@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+62892838399")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.USER)
                .build();
        User userSave = userRepository.save(user);

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        get("/api/v1/users/" + userSave.getId())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {

                    WebResponse<DetailUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());

                    Assertions.assertEquals(userSave.getId().toString(), response.getData().getId());
                    Assertions.assertEquals(userSave.getName(), response.getData().getName());
                    Assertions.assertEquals(userSave.getEmail(), response.getData().getEmail());
                    Assertions.assertEquals(userSave.getImageUrl(), response.getData().getImageUrl());
                    Assertions.assertEquals(dateTimeUtils.localDateFromInstantZoneJakarta(userSave.getDateOfBirth()).toString(), response.getData().getDob());
                    Assertions.assertEquals(userSave.getPhoneNumber(), response.getData().getPhone());
                    Assertions.assertEquals(userSave.getIsActive(), response.getData().isActive());
                    Assertions.assertNull(response.getData().getRole());
                    Assertions.assertEquals(LocalDateTime.ofInstant(userSave.getCreatedAt(), ZoneId.of("Asia/Jakarta")).toString(), response.getData().getDateCreated());

                });

    }

    @Test
    void getDetailUserByAdminErrorNotFoundTest() throws Exception {

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        get("/api/v1/users/not-found")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isNotFound())
                .andExpectAll(result -> {

                    WebResponse<DetailUserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

                    Assertions.assertEquals("user not found", response.getError());

                });

    }

    @Test
    void deleteUserSuccessTest() throws Exception {

        User user = User.builder()
                .name("User")
                .email("user@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+62892838399")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        delete("/api/v1/users/" + user.getId())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isOk())
                .andExpectAll(result -> {

                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNull(response.getError());
                    Assertions.assertEquals("success delete data user", response.getData());

                });

    }

    @Test
    void deleteUserAccessForbiddenErrorTest() throws Exception {

        User user = User.builder()
                .name("User")
                .email("user@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+62892838399")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.USER)
                .build();

        User user2 = User.builder()
                .name("User")
                .email("user2@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+628922838399")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.USER)
                .build();

        userRepository.save(user);
        User user2Saved = userRepository.save(user2);

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        delete("/api/v1/users/" + user2Saved.getId())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isForbidden())
                .andExpectAll(result -> {

                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(response.getData());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertEquals("don't have a access", response.getError());

                });

    }


    @Test
    void deleteUserNotFoundErrorTest() throws Exception {

        User user = User.builder()
                .name("User")
                .email("user@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+62892838399")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.USER)
                .build();

        User userSave = userRepository.save(user);
        userRepository.deleteById(userSave.getId());

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        delete("/api/v1/users/" + userSave.getId())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isNotFound())
                .andExpectAll(result -> {

                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(response.getData());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertEquals("user not found", response.getError());

                });
    }

    @Test
    void deleteUserNotFoundIdRandomErrorTest() throws Exception {

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        delete("/api/v1/users/not-found")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isNotFound())
                .andExpectAll(result -> {

                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(response.getData());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertEquals("user not found", response.getError());

                });
    }

    @Test
    void getListDataUserPagingSuccessTest() throws Exception {

        for (int i = 1; i <= 50; i++) {
            User user = User.builder()
                    .email("user" + i + "@gmail.com")
                    .name("user " + i)
                    .password("secretpassword")
                    .phoneNumber("+62883748473" + i)
                    .role(UserRoleEnum.USER)
                    .build();

            userRepository.save(user);
        }

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                get("/api/v1/users?page=1&size=10")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andExpectAll(
                result -> {
                    WebResponsePaging<List<UserResponse>> responsePaging = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(responsePaging.getError());
                    Assertions.assertNotNull(responsePaging.getData());
                    Assertions.assertEquals(responsePaging.getStatus(), HttpStatus.OK.value());
                    Assertions.assertEquals(50, responsePaging.getTotalItem());
                    Assertions.assertEquals(1, responsePaging.getCurrentPage());
                    Assertions.assertEquals(10, responsePaging.getPerPage());
                    Assertions.assertEquals(5, responsePaging.getLastPage());


                }
        );

    }

    @Test
    void getListDataUserPagingQueryEmailSuccessTest() throws Exception {

        for (int i = 1; i <= 50; i++) {
            User user = User.builder()
                    .email("user" + i + "@gmail.com")
                    .name("user " + i)
                    .password("secretpassword")
                    .phoneNumber("+62883748473" + i)
                    .role(UserRoleEnum.USER)
                    .build();

            userRepository.save(user);
        }

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                get("/api/v1/users?page=1&size=10&email=user49")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andExpectAll(
                result -> {
                    WebResponsePaging<List<UserResponse>> responsePaging = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(responsePaging.getError());
                    Assertions.assertNotNull(responsePaging.getData());
                    Assertions.assertEquals(responsePaging.getStatus(), HttpStatus.OK.value());
                    Assertions.assertEquals(1, responsePaging.getTotalItem());
                    Assertions.assertEquals(1, responsePaging.getCurrentPage());
                    Assertions.assertEquals(10, responsePaging.getPerPage());
                    Assertions.assertEquals(1, responsePaging.getLastPage());
                    Assertions.assertEquals("user 49", responsePaging.getData().get(0).getName());
                    Assertions.assertEquals("user49@gmail.com", responsePaging.getData().get(0).getEmail());
                }
        );

    }

    @Test
    void getListDataUserPagingQueryNameSuccessTest() throws Exception {

        for (int i = 1; i <= 50; i++) {
            User user = User.builder()
                    .email("user" + i + "@gmail.com")
                    .name("user " + i)
                    .password("secretpassword")
                    .phoneNumber("+62883748473" + i)
                    .role(UserRoleEnum.USER)
                    .build();

            userRepository.save(user);
        }

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                get("/api/v1/users?page=1&size=10&name=10")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andExpectAll(
                result -> {
                    WebResponsePaging<List<UserResponse>> responsePaging = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(responsePaging.getError());
                    Assertions.assertNotNull(responsePaging.getData());
                    Assertions.assertEquals(responsePaging.getStatus(), HttpStatus.OK.value());
                    Assertions.assertEquals(1, responsePaging.getTotalItem());
                    Assertions.assertEquals(1, responsePaging.getCurrentPage());
                    Assertions.assertEquals(10, responsePaging.getPerPage());
                    Assertions.assertEquals(1, responsePaging.getLastPage());
                    Assertions.assertEquals("user 10", responsePaging.getData().get(0).getName());
                    Assertions.assertEquals("user10@gmail.com", responsePaging.getData().get(0).getEmail());
                }
        );

    }

    @Test
    void getListDataAdminPagingSuccessTest() throws Exception {

        for (int i = 1; i <= 50; i++) {
            User user = User.builder()
                    .email("admin" + i + "@gmail.com")
                    .name("admin " + i)
                    .password("secretpassword")
                    .phoneNumber("+62883348473" + i)
                    .role(UserRoleEnum.ADMIN)
                    .build();

            userRepository.save(user);
        }

        for (int i = 1; i <= 50; i++) {
            User user = User.builder()
                    .email("user" + i + "@gmail.com")
                    .name("user " + i)
                    .password("secretpassword")
                    .phoneNumber("+62883748473" + i)
                    .role(UserRoleEnum.USER)
                    .build();

            userRepository.save(user);
        }

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                get("/api/v1/users?page=1&size=10&role=admin")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andExpectAll(
                result -> {
                    WebResponsePaging<List<UserResponse>> responsePaging = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(responsePaging.getError());
                    Assertions.assertNotNull(responsePaging.getData());
                    Assertions.assertEquals(responsePaging.getStatus(), HttpStatus.OK.value());
                    Assertions.assertEquals(50, responsePaging.getTotalItem());
                    Assertions.assertEquals(1, responsePaging.getCurrentPage());
                    Assertions.assertEquals(10, responsePaging.getPerPage());
                    Assertions.assertEquals(5, responsePaging.getLastPage());

                    Assertions.assertEquals("admin 50", responsePaging.getData().get(0).getName());

                }
        );

    }

    @Test
    void getListDataUserPagingErrorForbiddenTest() throws Exception {

        for (int i = 1; i <= 50; i++) {
            User user = User.builder()
                    .email("user" + i + "@gmail.com")
                    .name("user " + i)
                    .password("secretpassword")
                    .phoneNumber("+62883748473" + i)
                    .role(UserRoleEnum.USER)
                    .build();

            userRepository.save(user);
        }

        User userData = User.builder()
                .email("usertest@gmail.com")
                .name("user")
                .password("secretpassword")
                .phoneNumber("+62883748473")
                .role(UserRoleEnum.USER)
                .build();

        userRepository.save(userData);

        String token = jwtService.generateToken(userData);

        mockMvc.perform(
                get("/api/v1/users?page=1&size10")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isForbidden()
        ).andExpectAll(
                result -> {
                    WebResponsePaging<List<UserResponse>> responsePaging = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(responsePaging.getError());
                    Assertions.assertNull(responsePaging.getData());

                    Assertions.assertEquals("don't have a access", responsePaging.getError());
                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), responsePaging.getStatus());
                }
        );

    }

    @Test
    void getUserTransactionsSuccessTest() throws Exception {

        User userData = User.builder()
                .email("usertest@gmail.com")
                .name("user")
                .password("secretpassword")
                .phoneNumber("+62883748473")
                .role(UserRoleEnum.USER)
                .build();

        User user = userRepository.save(userData);

        for (int i = 0; i < 20; i++) {
            Transaction transaction = Transaction.builder()
                    .noInvoice(UUID.randomUUID().toString())
                    .carName("Avanza")
                    .carImageUrl("url")
                    .carBrand(CarBrandEnum.TOYOTA)
                    .carYear(2020)
                    .carCapacity(6)
                    .carCc(2000)
                    .startDate(Instant.now())
                    .endDate(Instant.now().plusSeconds(2000))
                    .durationDay(1)
                    .carPrice(250000.0)
                    .totalPrice(250000.0)
                    .carTax(0)
                    .carDiscount(0)
                    .status(TransactionStatusEnum.FINISH)
                    .user(user)
                    .build();
            transactionRepository.save(transaction);
        }

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        get("/api/v1/users/transactions?page=1&size=10")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<UserTransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertEquals(10, response.getData().size());
                    Assertions.assertEquals(20, response.getTotalItem());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(10, response.getPerPage());
                    Assertions.assertEquals(2, response.getLastPage());
                });

    }
}
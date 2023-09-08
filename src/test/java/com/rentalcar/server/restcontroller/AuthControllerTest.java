package com.rentalcar.server.restcontroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalcar.server.model.RegisterRequest;
import com.rentalcar.server.model.WebResponse;
import com.rentalcar.server.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            Assertions.assertEquals("registration successful", response.getData());
        });

    }

    @Test
    void registerEmailNotValid() throws Exception{
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
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("format email is not valid", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerPhoneNumberNotValid() throws Exception{
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
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("phone number is not valid", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerPasswordError() throws Exception{
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
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("password must have 8 characters or more", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerPasswordIsBlankError() throws Exception{
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
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("password must not be blank", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerNameIsBlankError() throws Exception{
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
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("name must not be blank", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerPhoneIsBlankError() throws Exception{
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
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("phone must not be blank", response.getError());
            System.out.println(response.getError());
        });
    }

    @Test
    void registerEmailIsBlankError() throws Exception{
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
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            Assertions.assertNotNull(response.getError());
            Assertions.assertEquals("email must not be blank", response.getError());
            System.out.println(response.getError());
        });
    }
}
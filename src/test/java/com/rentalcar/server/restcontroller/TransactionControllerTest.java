package com.rentalcar.server.restcontroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalcar.server.entity.Car;
import com.rentalcar.server.entity.CarBrandEnum;
import com.rentalcar.server.entity.CarTransmissionEnum;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.TransactionCreateRequest;
import com.rentalcar.server.model.TransactionCreateResponse;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.repository.CarRepository;
import com.rentalcar.server.repository.TransactionRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.security.JwtService;
import com.rentalcar.server.service.AuthService;
import com.rentalcar.server.service.CarService;
import org.apache.tomcat.util.http.parser.Authorization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AuthService authService;

    @Autowired
    CarService carService;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ObjectMapper objectMapper;

    private User admin;
    private User user;
    private Car car;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        carRepository.deleteAll();
        transactionRepository.deleteAll();

        // create admin data
        admin = authService.createAdmin(User.builder()
                .name("Admin")
                .email("admin@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+628928383744")
                .build()
        );

        // create user data
        user = authService.createUser(User.builder()
                .name("User")
                .email("user@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+628928383745")
                .build()
        );

        // Create car dummy data
        car = carService.createDummyCar();

        // generate admin token
        adminToken = jwtService.generateToken(admin);

        // generate user token
        userToken = jwtService.generateToken(user);
    }

    @Test
    void createTransactionSuccess() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T10:00:00")
                .build();

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/transactions")
                        .header(AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTransactionRequest))

        )
                .andExpectAll(status().isCreated())
                .andExpectAll(result -> {
                    WebResponse<TransactionCreateResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(response.getStatus(), HttpStatus.CREATED.value());
                })
        ;
    }
}
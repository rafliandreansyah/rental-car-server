package com.rentalcar.server.restcontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalcar.server.entity.*;
import com.rentalcar.server.model.*;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.model.base.WebResponsePaging;
import com.rentalcar.server.repository.CarRentedRepository;
import com.rentalcar.server.repository.CarRepository;
import com.rentalcar.server.repository.TransactionRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.security.JwtService;
import com.rentalcar.server.service.AuthService;
import com.rentalcar.server.service.CarService;
import com.rentalcar.server.service.TransactionService;
import com.rentalcar.server.util.UUIDUtils;
import org.apache.tomcat.util.http.parser.Authorization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

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
    TransactionService transactionService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    CarRentedRepository carRentedRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UUIDUtils uuidUtils;

    private User admin;
    private User user;
    private Car car;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        carRentedRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

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
    void createTransactionSuccessTest() throws Exception {
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
                    WebResponse<TransactionCreateResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatus());
                });
    }

    @Test
    void createSuccessAdminCreateTransactionToOtherUserTest() throws Exception {


        TransactionCreateRequest createTransactionRequestToOtherUser = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T10:00:00")
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createTransactionRequestToOtherUser))

                )
                .andExpectAll(status().isCreated())
                .andExpectAll(result -> {
                    WebResponse<TransactionCreateResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatus());
                });
    }

    @Test
    void createTransactionErrorUserNotFoundTest() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString() + "dwad")
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createTransactionRequest))

                )
                .andExpectAll(status().isNotFound())
                .andExpectAll(result -> {
                    WebResponse<TransactionCreateResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals("user not found", response.getError());
                    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
                });
    }

    @Test
    void createTransactionErrorCarNotFoundTest() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString() + "dwad")
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createTransactionRequest))

                )
                .andExpectAll(status().isNotFound())
                .andExpectAll(result -> {
                    WebResponse<TransactionCreateResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals("car not found", response.getError());
                    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
                });
    }

    @Test
    void createTransactionErrorUserToOtherUserTest() throws Exception {
        TransactionCreateRequest createTransactionRequestToOtherUser = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(admin.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T10:00:00")
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createTransactionRequestToOtherUser))

                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<TransactionCreateResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals("cannot create transaction to other user", response.getError());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                });
    }

    @Test
    void createTransactionErrorCarNotAvailableTest() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateRequest createTransactionRequest1 = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T10:00:00")
                .build();

        transactionService.createTransaction(user, createTransactionRequest1);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createTransactionRequest))

                )
                .andExpectAll(status().isConflict())
                .andExpectAll(result -> {
                    WebResponse<TransactionCreateResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals("car is not available, please select another date", response.getError());
                    Assertions.assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
                });
    }

    @Test
    void createTransactionErrorUnAuthorizedTest() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createTransactionRequest))

                )
                .andExpectAll(status().isUnauthorized())
                .andExpectAll(result -> {
                    WebResponse<TransactionCreateResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals("authentication failed", response.getError());
                    Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
                });
    }

    @Test
    void getDetailTransactionSuccessTest() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions/" + transactionCreated.getId())
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<TransactionDetailResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(transactionCreated.getId(), response.getData().getId());
                    System.out.println(response.getData().toString());
                });
    }

    @Test
    void getDetailTransactionErrorTransactionNotFoundTest() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions/" + transactionCreated.getId() + "not-found")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isNotFound())
                .andExpectAll(result -> {
                    WebResponse<TransactionDetailResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
                    Assertions.assertEquals("transaction not found", response.getError());
                });
    }

    @Test
    void getDetailTransactionErrorAccessForbiddenGetOtherUserTransactionTest() throws Exception {

        // create user data
        User userData = authService.createUser(User.builder()
                .name("User2")
                .email("user2@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+628928381145")
                .build()
        );

        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(userData.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(userData, createTransactionRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions/" + transactionCreated.getId())
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponse<TransactionDetailResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
                    Assertions.assertEquals("don't have a access", response.getError());
                });
    }

    @Test
    void getListTransactionByAdminPerintisSuccessTest() throws Exception {

        for (int i = 0; i < 10; i++) {
            String date;
            int dateIncrement = (i + 1) + i + 1;
            if (dateIncrement < 10) {
                date = "0" + dateIncrement;
            } else {
                date = dateIncrement + "";
            }


            TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                    .carId(car.getId().toString())
                    .userId(user.getId().toString())
                    .duration(1)
                    .dateAndTime("2024-03-" + date + "T12:00:00")
                    .build();

            transactionService.createTransaction(user, createTransactionRequest);
        }

        // create admin data
        User adminPerintis = authService.createAdmin(User.builder()
                        .name("Admin Perintis")
                        .email("perintis@gmail.com")
                        .password("amaterasu")
                        .phoneNumber("+62892838223744")
                        .build());


        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + jwtService.generateToken(adminPerintis))
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<TransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getPerPage());
                    Assertions.assertNotNull(response.getTotalItem());
                    Assertions.assertNotNull(response.getCurrentPage());
                    Assertions.assertNotNull(response.getLastPage());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(10, response.getData().size());
                    Assertions.assertEquals(10, response.getPerPage());
                    Assertions.assertEquals(10, response.getTotalItem());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(1, response.getLastPage());
                });

    }

    @Test
    void getListTransactionByAdminSuccessTest() throws Exception {

        for (int i = 0; i < 10; i++) {
            String date;
            int dateIncrement = (i + 1) + i + 1;
            if (dateIncrement < 10) {
                date = "0" + dateIncrement;
            } else {
                date = dateIncrement + "";
            }


            TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                    .carId(car.getId().toString())
                    .userId(user.getId().toString())
                    .duration(1)
                    .dateAndTime("2024-03-" + date + "T12:00:00")
                    .build();

            transactionService.createTransaction(user, createTransactionRequest);
        }

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<TransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getPerPage());
                    Assertions.assertNotNull(response.getTotalItem());
                    Assertions.assertNotNull(response.getCurrentPage());
                    Assertions.assertNotNull(response.getLastPage());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(0, response.getData().size());
                    Assertions.assertEquals(10, response.getPerPage());
                    Assertions.assertEquals(0, response.getTotalItem());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(0, response.getLastPage());
                });

    }

    @Test
    void getListTransactionByAdminWithAuthorizationSuccessTest() throws Exception {

        for (int i = 0; i < 10; i++) {
            String date;
            int dateIncrement = (i + 1) + i + 1;
            if (dateIncrement < 10) {
                date = "0" + dateIncrement;
            } else {
                date = dateIncrement + "";
            }


            TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                    .carId(car.getId().toString())
                    .userId(user.getId().toString())
                    .duration(1)
                    .dateAndTime("2024-03-" + date + "T12:00:00")
                    .build();

            transactionService.createTransaction(user, createTransactionRequest);
        }

        /*
        * Create authorization access car
        * */
        CarCreateAuthorizationRequest carCreateAuthorizationRequest = CarCreateAuthorizationRequest.builder()
                .userId(List.of(admin.getId().toString()))
                .carId(List.of(car.getId().toString()))
                .build();

        carService.createCarAuthorization(admin, carCreateAuthorizationRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<TransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getPerPage());
                    Assertions.assertNotNull(response.getTotalItem());
                    Assertions.assertNotNull(response.getCurrentPage());
                    Assertions.assertNotNull(response.getLastPage());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(10, response.getData().size());
                    Assertions.assertEquals(10, response.getPerPage());
                    Assertions.assertEquals(10, response.getTotalItem());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(1, response.getLastPage());
                });

    }

    @Test
    void getListTransactionByAdminWithPagingSuccessTest() throws Exception {

        for (int i = 0; i < 10; i++) {
            String date;
            int dateIncrement = (i + 1) + i + 1;
            if (dateIncrement < 10) {
                date = "0" + dateIncrement;
            } else {
                date = dateIncrement + "";
            }


            TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                    .carId(car.getId().toString())
                    .userId(user.getId().toString())
                    .duration(1)
                    .dateAndTime("2024-03-" + date + "T12:00:00")
                    .build();

            transactionService.createTransaction(user, createTransactionRequest);
        }

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("size", "5")

                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<TransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getPerPage());
                    Assertions.assertNotNull(response.getTotalItem());
                    Assertions.assertNotNull(response.getCurrentPage());
                    Assertions.assertNotNull(response.getLastPage());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(0, response.getData().size());
                    Assertions.assertEquals(5, response.getPerPage());
                    Assertions.assertEquals(0, response.getTotalItem());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(0, response.getLastPage());
                });

    }

    @Test
    void getListTransactionByAdminWithPagingAndAuthorizationSuccessTest() throws Exception {

        for (int i = 0; i < 10; i++) {
            String date;
            int dateIncrement = (i + 1) + i + 1;
            if (dateIncrement < 10) {
                date = "0" + dateIncrement;
            } else {
                date = dateIncrement + "";
            }


            TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                    .carId(car.getId().toString())
                    .userId(user.getId().toString())
                    .duration(1)
                    .dateAndTime("2024-03-" + date + "T12:00:00")
                    .build();

            transactionService.createTransaction(user, createTransactionRequest);
        }

        /*
         * Create authorization access car
         * */
        CarCreateAuthorizationRequest carCreateAuthorizationRequest = CarCreateAuthorizationRequest.builder()
                .userId(List.of(admin.getId().toString()))
                .carId(List.of(car.getId().toString()))
                .build();

        carService.createCarAuthorization(admin, carCreateAuthorizationRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("size", "5")

                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<TransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getPerPage());
                    Assertions.assertNotNull(response.getTotalItem());
                    Assertions.assertNotNull(response.getCurrentPage());
                    Assertions.assertNotNull(response.getLastPage());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(5, response.getData().size());
                    Assertions.assertEquals(5, response.getPerPage());
                    Assertions.assertEquals(10, response.getTotalItem());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(2, response.getLastPage());
                });

    }

    @Test
    void getListTransactionByAdminFilterByDateSuccessTest() throws Exception {

        for (int i = 0; i < 10; i++) {
            String date;
            int dateIncrement = (i + 1) + i + 1;
            if (dateIncrement < 10) {
                date = "0" + dateIncrement;
            } else {
                date = dateIncrement + "";
            }


            TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                    .carId(car.getId().toString())
                    .userId(user.getId().toString())
                    .duration(1)
                    .dateAndTime("2024-03-" + date + "T12:00:00")
                    .build();

            transactionService.createTransaction(user, createTransactionRequest);
        }

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("start_date", "2024-03-01")
                                .param("end_date", "2024-03-10")
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<TransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getPerPage());
                    Assertions.assertNotNull(response.getTotalItem());
                    Assertions.assertNotNull(response.getCurrentPage());
                    Assertions.assertNotNull(response.getLastPage());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(0, response.getData().size());
                    Assertions.assertEquals(10, response.getPerPage());
                    Assertions.assertEquals(0, response.getTotalItem());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(0, response.getLastPage());
                });

    }

    @Test
    void getListTransactionByAdminFilterByDateWithAuthorizationSuccessTest() throws Exception {

        for (int i = 0; i < 10; i++) {
            String date;
            int dateIncrement = (i + 1) + i + 1;
            if (dateIncrement < 10) {
                date = "0" + dateIncrement;
            } else {
                date = dateIncrement + "";
            }


            TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                    .carId(car.getId().toString())
                    .userId(user.getId().toString())
                    .duration(1)
                    .dateAndTime("2024-03-" + date + "T12:00:00")
                    .build();

            transactionService.createTransaction(user, createTransactionRequest);
        }

        /*
         * Create authorization access car
         * */
        CarCreateAuthorizationRequest carCreateAuthorizationRequest = CarCreateAuthorizationRequest.builder()
                .userId(List.of(admin.getId().toString()))
                .carId(List.of(car.getId().toString()))
                .build();

        carService.createCarAuthorization(admin, carCreateAuthorizationRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("start_date", LocalDateTime.now().toString())
                                .param("end_date", LocalDateTime.now().plusDays(10).toString())
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<TransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getPerPage());
                    Assertions.assertNotNull(response.getTotalItem());
                    Assertions.assertNotNull(response.getCurrentPage());
                    Assertions.assertNotNull(response.getLastPage());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(10, response.getData().size());
                    Assertions.assertEquals(10, response.getPerPage());
                    Assertions.assertEquals(10, response.getTotalItem());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(1, response.getLastPage());
                });

    }

    @Test
    void getListTransactionByAdminFilterByDateErrorTest() throws Exception {

        for (int i = 0; i < 10; i++) {
            String date;
            int dateIncrement = (i + 1) + i + 1;
            if (dateIncrement < 10) {
                date = "0" + dateIncrement;
            } else {
                date = dateIncrement + "";
            }


            TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                    .carId(car.getId().toString())
                    .userId(user.getId().toString())
                    .duration(1)
                    .dateAndTime("2024-03-" + date + "T12:00:00")
                    .build();

            transactionService.createTransaction(user, createTransactionRequest);
        }

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("start_date", "2024-03-01")
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponsePaging<List<TransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                });

    }

    @Test
    void getListTransactionByUserForbiddenAccessErrorTest() throws Exception {

        for (int i = 0; i < 10; i++) {
            String date;
            int dateIncrement = (i + 1) + i + 1;
            if (dateIncrement < 10) {
                date = "0" + dateIncrement;
            } else {
                date = dateIncrement + "";
            }


            TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                    .carId(car.getId().toString())
                    .userId(user.getId().toString())
                    .duration(1)
                    .dateAndTime("2024-03-" + date + "T12:00:00")
                    .build();

            transactionService.createTransaction(user, createTransactionRequest);
        }

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/transactions")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponsePaging<List<TransactionResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
                });

    }

    @Test
    void deleteTransactionUserByIdSuccess() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/transactions/{id}", transactionCreated.getId())
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals("success delete transaction", response.getData());

                });

    }

    @Test
    void deleteTransactionAdminByIdSuccess() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/transactions/" + transactionCreated.getId())
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals("success delete transaction", response.getData());

                });
    }

    @Test
    void deleteTransactionNotFoundError() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/transactions/" + transactionCreated.getId() + "not-found")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isNotFound())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
                    Assertions.assertEquals("transaction not found", response.getError());

                });
    }

    @Test
    void deleteTransactionAccessForbiddenError() throws Exception {
        // create user data
        User userData = authService.createUser(User.builder()
                .name("User2")
                .email("user2@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+628928381145")
                .build()
        );

        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(userData.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(userData, createTransactionRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/transactions/" + transactionCreated.getId())
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
                    Assertions.assertEquals("don't have a access", response.getError());

                });
    }

    @Test
    void deleteTransactionAccessForbiddenStatusPaymentError() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        //Edit transaction
        transactionService.editTransaction(admin, transactionCreated.getId(), 0, null);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/transactions/" + transactionCreated.getId())
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
                    Assertions.assertEquals("cannot delete transaction because payment has already been made", response.getError());

                });
    }

    @Test
    void editTransactionUserSuccessTest() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "payment_image",
                "rafli.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/transactions/{id}", transactionCreated.getId())
                                .file(mockMultipartFile)
                                .with(request -> {
                                    request.setMethod(HttpMethod.PATCH.name());
                                    return request;
                                })
                                .param("status", "0")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<TransactionEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(0, response.getData().getStatus());
                });
    }

    @Test
    void editTransactionAdminSuccessTest() throws Exception{
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/transactions/{id}", transactionCreated.getId())
                                .with(request -> {
                                    request.setMethod(HttpMethod.PATCH.name());
                                    return request;
                                })
                                .param("status", "1")
                                .header(AUTHORIZATION, "Bearer " + adminToken)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<TransactionEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(1, response.getData().getStatus());
                });
    }


    @Test
    void editTransactionUserNotFoundErrorTest() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "payment_image",
                "rafli.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/transactions/{id}", transactionCreated.getId() + "not-found")
                                .file(mockMultipartFile)
                                .with(request -> {
                                    request.setMethod(HttpMethod.PATCH.name());
                                    return request;
                                })
                                .param("status", "0")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isNotFound())
                .andExpectAll(result -> {
                    WebResponse<TransactionEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
                    Assertions.assertEquals("transaction not found", response.getError());
                });
    }

    @Test
    void editTransactionUserStatusNotFoundErrorTest() throws Exception {
        TransactionCreateRequest createTransactionRequest = TransactionCreateRequest.builder()
                .carId(car.getId().toString())
                .userId(user.getId().toString())
                .duration(1)
                .dateAndTime("2024-03-04T12:00:00")
                .build();

        TransactionCreateResponse transactionCreated = transactionService.createTransaction(user, createTransactionRequest);

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "payment_image",
                "rafli.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/transactions/{id}", transactionCreated.getId())
                                .file(mockMultipartFile)
                                .with(request -> {
                                    request.setMethod(HttpMethod.PATCH.name());
                                    return request;
                                })
                                .param("status", "10")
                                .header(AUTHORIZATION, "Bearer " + userToken)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<TransactionEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("status not found", response.getError());
                });
    }

}
package com.rentalcar.server.restcontroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalcar.server.entity.*;
import com.rentalcar.server.model.*;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.model.base.WebResponsePaging;
import com.rentalcar.server.repository.*;
import com.rentalcar.server.security.JwtService;
import com.rentalcar.server.service.AuthService;
import com.rentalcar.server.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
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

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class CarControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CarRepository carRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CarImageDetailRepository carImageDetailRepository;

    @Autowired
    CarAuthorizationRepository carAuthorizationRepository;

    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    AuthService authService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    ObjectMapper objectMapper;

    private User admin;

    private User user;

    private Car car;

    @BeforeEach
    void setUp() {

        ratingRepository.deleteAll();
        carImageDetailRepository.deleteAll();
        userRepository.deleteAll();
        carRepository.deleteAll();

        admin = authService.createAdmin(
                User.builder()
                        .name("Admin")
                        .email("admin@yahoo.com")
                        .password("amaterasu")
                        .phoneNumber("+628928383744")
                        .build()
        );
        user = User.builder()
                .name("User")
                .email("user@yahoo.com")
                .password("amaterasu")
                .phoneNumber("+62892838399")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.USER)
                .build();
        userRepository.save(user);

        // Create car dummy data
        car = Car.builder()
                .name("Dummy Car")
                .imageUrl("testing_image")
                .brand(CarBrandEnum.TOYOTA)
                .year(2022)
                .capacity(6)
                .cc(2000)
                .pricePerDay(200_000D)
                .tax(0)
                .discount(0)
                .description("dummy car data")
                .transmission(CarTransmissionEnum.AT)
                .tax(0)
                .discount(0)
                .build();
        carRepository.save(car);
    }

    @AfterEach
    void afterTest() {
        ratingRepository.deleteAll();
        carImageDetailRepository.deleteAll();
        userRepository.deleteAll();
        carRepository.deleteAll();
    }

    @Test
    void getDetailCarSuccessTest() throws Exception {
        Car car = Car.builder()
                .name("Avanza")
                .brand(CarBrandEnum.TOYOTA)
                .capacity(6)
                .year(2020)
                .transmission(CarTransmissionEnum.AT)
                .pricePerDay(250000.0)
                .imageUrl("url")
                .cc(2000)
                .description("description")
                .discount(0)
                .tax(0)
                .build();
        Car saveCar = carRepository.save(car);

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        get("/api/v1/cars/" + saveCar.getId().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<CarDetailResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());

                    CarDetailResponse responseCarData = response.getData();
                    Assertions.assertEquals(car.getId().toString(), responseCarData.getId());
                    Assertions.assertEquals(car.getName(), responseCarData.getName());
                    Assertions.assertEquals(car.getImageUrl(), responseCarData.getImage());
                    Assertions.assertEquals(car.getBrand().name(), responseCarData.getBrand());
                    Assertions.assertEquals(car.getYear(), responseCarData.getYear());
                    Assertions.assertEquals(car.getCapacity(), responseCarData.getCapacity());
                    Assertions.assertEquals(car.getCc(), responseCarData.getCc());
                    Assertions.assertEquals(car.getPricePerDay(), responseCarData.getPricePerDay());
                    Assertions.assertEquals(car.getTax(), responseCarData.getTax());
                    Assertions.assertEquals(car.getDiscount(), responseCarData.getDiscount());
                    Assertions.assertEquals(car.getDescription(), responseCarData.getDescription());
                    Assertions.assertEquals(car.getTransmission().equals(CarTransmissionEnum.MT) ? "manual" : "automatic", responseCarData.getTransmission());
                    Assertions.assertEquals(car.getIsActive(), responseCarData.getIsActive());
                });
    }

    @Test
    void getDetailCarWithRatingSuccessTest() throws Exception {
        Car car = Car.builder()
                .name("Avanza")
                .brand(CarBrandEnum.TOYOTA)
                .capacity(6)
                .year(2020)
                .transmission(CarTransmissionEnum.AT)
                .pricePerDay(250000.0)
                .imageUrl("url")
                .cc(2000)
                .description("description")
                .discount(0)
                .tax(0)
                .build();
        Car saveCar = carRepository.save(car);

        for (int i = 0; i < 10; i++) {
            ratingRepository.save(Rating.builder()
                    .rating(10.0)
                    .comment("Good")
                    .user(user)
                    .car(saveCar)
                    .build()
            );
        }

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        get("/api/v1/cars/" + saveCar.getId().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<CarDetailResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());

                    CarDetailResponse responseCarData = response.getData();
                    Assertions.assertEquals(car.getId().toString(), responseCarData.getId());
                    Assertions.assertEquals(car.getName(), responseCarData.getName());
                    Assertions.assertEquals(car.getImageUrl(), responseCarData.getImage());
                    Assertions.assertEquals(car.getBrand().name(), responseCarData.getBrand());
                    Assertions.assertEquals(car.getYear(), responseCarData.getYear());
                    Assertions.assertEquals(car.getCapacity(), responseCarData.getCapacity());
                    Assertions.assertEquals(car.getCc(), responseCarData.getCc());
                    Assertions.assertEquals(car.getPricePerDay(), responseCarData.getPricePerDay());
                    Assertions.assertEquals(car.getTax(), responseCarData.getTax());
                    Assertions.assertEquals(car.getDiscount(), responseCarData.getDiscount());
                    Assertions.assertEquals(car.getDescription(), responseCarData.getDescription());
                    Assertions.assertEquals(car.getTransmission().equals(CarTransmissionEnum.MT) ? "manual" : "automatic", responseCarData.getTransmission());
                    Assertions.assertEquals(car.getIsActive(), responseCarData.getIsActive());
                    Assertions.assertEquals(10.0, responseCarData.getRating());
                    Assertions.assertEquals(10, responseCarData.getTotalReview());
                });
    }

    @Test
    void getDetailUnAuthorizedErrorTest() throws Exception {
        Car car = Car.builder()
                .name("Avanza")
                .brand(CarBrandEnum.TOYOTA)
                .capacity(6)
                .year(2020)
                .transmission(CarTransmissionEnum.AT)
                .pricePerDay(250000.0)
                .imageUrl("url")
                .cc(2000)
                .description("description")
                .discount(0)
                .tax(0)
                .build();
        Car saveCar = carRepository.save(car);

        mockMvc.perform(
                        get("/api/v1/cars/" + saveCar.getId().toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isUnauthorized())
                .andExpectAll(result -> {
                    WebResponse<CarDetailResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

                    Assertions.assertEquals("authentication failed", response.getError());
                });
    }

    @Test
    void getDetailCarNotFoundErrorTest() throws Exception {

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        get("/api/v1/cars/not-found")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isNotFound())
                .andExpectAll(result -> {
                    WebResponse<CarDetailResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

                    Assertions.assertEquals("car not found", response.getError());
                });
    }

    @Test
    void deleteCarSuccessTest() throws Exception {

        Car car = Car.builder()
                .name("Avanza")
                .brand(CarBrandEnum.TOYOTA)
                .capacity(6)
                .year(2020)
                .transmission(CarTransmissionEnum.AT)
                .pricePerDay(250000.0)
                .imageUrl("url")
                .cc(2000)
                .description("description")
                .discount(0)
                .tax(0)
                .build();
        Car saveCar = carRepository.save(car);

        CarAuthorization carAuthorization = CarAuthorization.builder()
                .car(saveCar)
                .user(admin)
                .build();

        carAuthorizationRepository.save(carAuthorization);

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        delete("/api/v1/cars/" + saveCar.getId().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals("success delete car", response.getData());
                });

    }

    @Test
    void deleteCarNotFoundErrorTest() throws Exception {

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        delete("/api/v1/cars/not-found")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isNotFound())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
                    Assertions.assertEquals("car not found", response.getError());
                });

    }

    @Test
    void deleteCarForbiddenErrorTest() throws Exception {

        Car car = Car.builder()
                .name("Avanza")
                .brand(CarBrandEnum.TOYOTA)
                .capacity(6)
                .year(2020)
                .transmission(CarTransmissionEnum.AT)
                .pricePerDay(250000.0)
                .imageUrl("url")
                .cc(2000)
                .description("description")
                .discount(0)
                .tax(0)
                .build();
        Car saveCar = carRepository.save(car);

        CarAuthorization carAuthorization = CarAuthorization.builder()
                .car(saveCar)
                .user(user)
                .build();

        carAuthorizationRepository.save(carAuthorization);

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        delete("/api/v1/cars/" + saveCar.getId().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
                    Assertions.assertEquals("don't have a access", response.getError());
                });

    }

    @Test
    void deleteCarNoAuthorizationCarErrorTest() throws Exception {

        Car car = Car.builder()
                .name("Avanza")
                .brand(CarBrandEnum.TOYOTA)
                .capacity(6)
                .year(2020)
                .transmission(CarTransmissionEnum.AT)
                .pricePerDay(250000.0)
                .imageUrl("url")
                .cc(2000)
                .description("description")
                .discount(0)
                .tax(0)
                .build();
        Car saveCar = carRepository.save(car);

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        delete("/api/v1/cars/" + saveCar.getId().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
                    Assertions.assertEquals("don't have authorization for this car", response.getError());
                });

    }

    @Test
    void createCarNoImageDetailSuccessTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );

        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .luggage(2)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .param("luggage", carCreateRequest.getLuggage().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isCreated())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    var data = response.getData();
                    Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatus());
                    Assertions.assertNotNull(data.getImage());
                    Assertions.assertEquals(carCreateRequest.getName(), data.getName());
                    Assertions.assertEquals(carCreateRequest.getPrice(), data.getPrice());
                    Assertions.assertEquals(carCreateRequest.getCc(), data.getCc());
                    Assertions.assertEquals(carCreateRequest.getCapacity(), data.getCapacity());
                    Assertions.assertEquals(carCreateRequest.getBrand().toUpperCase(), data.getBrand());
                    Assertions.assertEquals(carCreateRequest.getYear(), data.getYear());
                    Assertions.assertEquals(carCreateRequest.getDescription(), data.getDescription());
                    Assertions.assertEquals(carCreateRequest.getTransmission().toUpperCase(), data.getTransmission());
                    Assertions.assertEquals(carCreateRequest.getTax(), data.getTax());
                    Assertions.assertEquals(carCreateRequest.getDiscount(), data.getDiscount());
                    Assertions.assertEquals(carCreateRequest.getLuggage(), data.getLuggage());
                });
    }

    @Test
    void createCarWithImageDetailSuccessTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );

        List<MockMultipartFile> multipartFiles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MockMultipartFile multipartImageDetail = new MockMultipartFile(
                    "image_detail",
                    "car" + i + ".jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    ("my-images" + i).getBytes()
            );
            multipartFiles.add(multipartImageDetail);
        }

        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .file(multipartFiles.get(0))
                                .file(multipartFiles.get(1))
                                .file(multipartFiles.get(2))
                                .file(multipartFiles.get(3))
                                .file(multipartFiles.get(4))
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isCreated())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    var data = response.getData();
                    Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatus());
                    Assertions.assertNotNull(data.getImage());
                    Assertions.assertEquals(carCreateRequest.getName(), data.getName());
                    Assertions.assertEquals(carCreateRequest.getPrice(), data.getPrice());
                    Assertions.assertEquals(carCreateRequest.getCc(), data.getCc());
                    Assertions.assertEquals(carCreateRequest.getCapacity(), data.getCapacity());
                    Assertions.assertEquals(carCreateRequest.getBrand().toUpperCase(), data.getBrand());
                    Assertions.assertEquals(carCreateRequest.getYear(), data.getYear());
                    Assertions.assertEquals(carCreateRequest.getDescription(), data.getDescription());
                    Assertions.assertEquals(carCreateRequest.getTransmission().toUpperCase(), data.getTransmission());
                    Assertions.assertEquals(carCreateRequest.getTax(), data.getTax());
                    Assertions.assertEquals(carCreateRequest.getDiscount(), data.getDiscount());
                    Assertions.assertEquals(5, data.getImageDetail().size());

                    System.out.println(data.getImageDetail().toString());
                });
    }

    @Test
    void createCarForbiddenErrorTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );

        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
                    Assertions.assertEquals("don't have a access", response.getError());
                });
    }

    @Test
    void createCarNoImageErrorTest() throws Exception {
        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("image must not be blank", response.getError());
                });
    }

    @Test
    void createCarNoNameErrorTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );
        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("name must not be blank", response.getError());
                });
    }

    @Test
    void createCarNoPriceErrorTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );
        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("name", carCreateRequest.getName())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("price must not be blank", response.getError());
                });
    }

    @Test
    void createCarNoBrandErrorTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );
        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("brand must not be blank", response.getError());
                });
    }

    @Test
    void createCarNoYearErrorTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );
        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("year must not be blank", response.getError());
                });
    }

    @Test
    void createCarNoCapacityErrorTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );
        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("capacity must not be blank", response.getError());
                });
    }

    @Test
    void createCarNoCcErrorTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );
        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("cc must not be blank", response.getError());
                });
    }

    @Test
    void createCarNoDescriptionErrorTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );
        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("transmission", carCreateRequest.getTransmission())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("description must not be blank", response.getError());
                });
    }

    @Test
    void createCarNoTransmissionErrorTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );
        CarCreateRequest carCreateRequest = CarCreateRequest.builder()
                .name("avanza")
                .price(200000.00)
                .brand("toyota")
                .year(2020)
                .capacity(6)
                .cc(2000)
                .description("this is a avanza car new version")
                .transmission("at")
                .discount(0)
                .tax(0)
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars")
                                .file(multipartFile)
                                .param("name", carCreateRequest.getName())
                                .param("price", carCreateRequest.getPrice().toString())
                                .param("brand", carCreateRequest.getBrand())
                                .param("year", carCreateRequest.getYear().toString())
                                .param("capacity", carCreateRequest.getCapacity().toString())
                                .param("cc", carCreateRequest.getCc().toString())
                                .param("description", carCreateRequest.getDescription())
                                .param("discount", carCreateRequest.getDiscount().toString())
                                .param("tax", carCreateRequest.getTax().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());
                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("transmission must not be blank", response.getError());
                });
    }

    @Test
    void editCarWithOutImageSuccessTest() throws Exception {

        CarEditRequest carEditRequest = CarEditRequest
                .builder()
                .name("Edited Car")
                .brand(CarBrandEnum.TOYOTA.name())
                .year(2024)
                .capacity(4)
                .cc(3000)
                .price(400_000D)
                .tax(1)
                .luggage(2)
                .discount(10)
                .description("edited car data")
                .transmission(CarTransmissionEnum.MT.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars/{id}", car.getId())
                                .with(request -> {
                                    request.setMethod(HttpMethod.PATCH.name());
                                    return request;
                                })
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .param("name", carEditRequest.getName())
                                .param("brand", carEditRequest.getBrand())
                                .param("year", carEditRequest.getYear().toString())
                                .param("capacity", carEditRequest.getCapacity().toString())
                                .param("cc", carEditRequest.getCc().toString())
                                .param("price", carEditRequest.getPrice().toString())
                                .param("description", carEditRequest.getDescription())
                                .param("transmission", carEditRequest.getTransmission())
                                .param("tax", carEditRequest.getTax().toString())
                                .param("discount", carEditRequest.getDiscount().toString())
                                .param("luggage", carEditRequest.getLuggage().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(carEditRequest.getName(), response.getData().getName());
                    Assertions.assertEquals(carEditRequest.getBrand(), response.getData().getBrand());
                    Assertions.assertEquals(carEditRequest.getYear(), response.getData().getYear());
                    Assertions.assertEquals(carEditRequest.getCapacity(), response.getData().getCapacity());
                    Assertions.assertEquals(carEditRequest.getCc(), response.getData().getCc());
                    Assertions.assertEquals(carEditRequest.getDescription(), response.getData().getDescription());
                    Assertions.assertEquals(carEditRequest.getTransmission(), response.getData().getTransmission());
                    Assertions.assertEquals(carEditRequest.getTax(), response.getData().getTax());
                    Assertions.assertEquals(carEditRequest.getDiscount(), response.getData().getDiscount());
                    Assertions.assertEquals(carEditRequest.getPrice(), response.getData().getPrice());
                    Assertions.assertEquals(carEditRequest.getLuggage(), response.getData().getLuggage());
                });
    }

    @Test
    void editCarWithImageSuccessTest() throws Exception {

        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "car.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "my-images".getBytes()
        );

        List<MockMultipartFile> multipartFiles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MockMultipartFile multipartImageDetail = new MockMultipartFile(
                    "image_detail",
                    "car" + i + ".jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    ("my-images" + i).getBytes()
            );
            multipartFiles.add(multipartImageDetail);
        }

        CarEditRequest carEditRequest = CarEditRequest
                .builder()
                .name("Edited Car")
                .brand(CarBrandEnum.TOYOTA.name())
                .year(2024)
                .capacity(4)
                .cc(3000)
                .price(400_000D)
                .tax(1)
                .discount(10)
                .description("edited car data")
                .transmission(CarTransmissionEnum.MT.name())
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars/{id}", car.getId())
                                .file(multipartFile)
                                .file(multipartFiles.get(0))
                                .file(multipartFiles.get(1))
                                .file(multipartFiles.get(2))
                                .file(multipartFiles.get(3))
                                .file(multipartFiles.get(4))
                                .with(request -> {
                                    request.setMethod(HttpMethod.PATCH.name());
                                    return request;
                                })
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .param("name", carEditRequest.getName())
                                .param("brand", carEditRequest.getBrand())
                                .param("year", carEditRequest.getYear().toString())
                                .param("capacity", carEditRequest.getCapacity().toString())
                                .param("cc", carEditRequest.getCc().toString())
                                .param("price", carEditRequest.getPrice().toString())
                                .param("description", carEditRequest.getDescription())
                                .param("transmission", carEditRequest.getTransmission())
                                .param("tax", carEditRequest.getTax().toString())
                                .param("discount", carEditRequest.getDiscount().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());
                    Assertions.assertNotNull(response.getData().getImageDetail());
                    Assertions.assertNotNull(response.getData().getImage());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(carEditRequest.getName(), response.getData().getName());
                    Assertions.assertEquals(carEditRequest.getBrand(), response.getData().getBrand());
                    Assertions.assertEquals(carEditRequest.getYear(), response.getData().getYear());
                    Assertions.assertEquals(carEditRequest.getCapacity(), response.getData().getCapacity());
                    Assertions.assertEquals(carEditRequest.getCc(), response.getData().getCc());
                    Assertions.assertEquals(carEditRequest.getDescription(), response.getData().getDescription());
                    Assertions.assertEquals(carEditRequest.getTransmission(), response.getData().getTransmission());
                    Assertions.assertEquals(carEditRequest.getTax(), response.getData().getTax());
                    Assertions.assertEquals(carEditRequest.getDiscount(), response.getData().getDiscount());
                    Assertions.assertEquals(carEditRequest.getPrice(), response.getData().getPrice());
                    Assertions.assertEquals(5, response.getData().getImageDetail().size());
                });
    }

    @Test
    void editCarWithOutImageDontHaveAccessErrorTest() throws Exception {

        CarEditRequest carEditRequest = CarEditRequest
                .builder()
                .name("Edited Car")
                .brand(CarBrandEnum.TOYOTA.name())
                .year(2024)
                .capacity(4)
                .cc(3000)
                .price(400_000D)
                .tax(1)
                .discount(10)
                .description("edited car data")
                .transmission(CarTransmissionEnum.MT.name())
                .build();

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/cars/{id}", car.getId())
                                .with(request -> {
                                    request.setMethod(HttpMethod.PATCH.name());
                                    return request;
                                })
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .param("name", carEditRequest.getName())
                                .param("brand", carEditRequest.getBrand())
                                .param("year", carEditRequest.getYear().toString())
                                .param("capacity", carEditRequest.getCapacity().toString())
                                .param("cc", carEditRequest.getCc().toString())
                                .param("price", carEditRequest.getPrice().toString())
                                .param("description", carEditRequest.getDescription())
                                .param("transmission", carEditRequest.getTransmission())
                                .param("tax", carEditRequest.getTax().toString())
                                .param("discount", carEditRequest.getDiscount().toString())
                                .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponse<CarCreateAndEditResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
                    Assertions.assertEquals("don't have a access", response.getError());
                });
    }

    @Test
    void getListCarAdminSuccessTest() throws Exception {
        List<CarAuthorization> carAuthorizations = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            // Create car dummy data
            Car carData = Car.builder()
                    .name("Dummy Car " + i)
                    .imageUrl("testing_image")
                    .brand(CarBrandEnum.TOYOTA)
                    .year(2022)
                    .capacity(6)
                    .cc(2000)
                    .pricePerDay(200_000D)
                    .tax(0)
                    .discount(0)
                    .description("dummy car data")
                    .transmission(CarTransmissionEnum.AT)
                    .tax(0)
                    .discount(0)
                    .build();
            Car savedCar = carRepository.save(carData);
            carAuthorizations.add(
                    CarAuthorization.builder()
                            .user(admin)
                            .car(savedCar)
                            .build()
            );
        }
        carAuthorizationRepository.saveAll(carAuthorizations.stream().toList());

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/cars")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<CarResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponsePaging<List<CarResponse>>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(2, response.getLastPage());
                    Assertions.assertEquals(20, response.getPerPage());
                    Assertions.assertEquals(30, response.getTotalItem());
                    Assertions.assertEquals(20, response.getData().size());

                });
    }

    @Test
    void getListCarAdminWithRatingSuccessTest() throws Exception {
        List<CarAuthorization> carAuthorizations = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            // Create car dummy data
            Car carData = Car.builder()
                    .name("Dummy Car " + i)
                    .imageUrl("testing_image")
                    .brand(CarBrandEnum.TOYOTA)
                    .year(2022)
                    .capacity(6)
                    .cc(2000)
                    .pricePerDay(200_000D)
                    .tax(0)
                    .discount(0)
                    .description("dummy car data")
                    .transmission(CarTransmissionEnum.AT)
                    .tax(0)
                    .discount(0)
                    .build();
            Car savedCar = carRepository.save(carData);
            carAuthorizations.add(
                    CarAuthorization.builder()
                            .user(admin)
                            .car(savedCar)
                            .build()
            );

            ratingRepository.save(Rating.builder()
                    .rating(10.0)
                    .comment("Good")
                    .user(user)
                    .car(savedCar)
                    .build()
            );
        }
        carAuthorizationRepository.saveAll(carAuthorizations.stream().toList());

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/cars")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<CarResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponsePaging<List<CarResponse>>>() {
                    });

                    System.out.println(response.getData().get(0).toString());

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(2, response.getLastPage());
                    Assertions.assertEquals(20, response.getPerPage());
                    Assertions.assertEquals(30, response.getTotalItem());
                    Assertions.assertEquals(20, response.getData().size());
                    Assertions.assertEquals(10.0, response.getData().get(0).getRating());

                });
    }

    @Test
    void getListCarAdminPerintisSuccessTest() throws Exception {
        for (int i = 0; i < 30; i++) {
            // Create car dummy data
            Car carData = Car.builder()
                    .name("Dummy Car " + i)
                    .imageUrl("testing_image")
                    .brand(CarBrandEnum.TOYOTA)
                    .year(2022)
                    .capacity(6)
                    .cc(2000)
                    .pricePerDay(200_000D)
                    .tax(0)
                    .discount(0)
                    .description("dummy car data")
                    .transmission(i % 2 == 0 ? CarTransmissionEnum.AT : CarTransmissionEnum.MT)
                    .tax(0)
                    .discount(0)
                    .build();
            Car savedCar = carRepository.save(carData);
        }

        User perintis = User.builder()
                .name("Perintis")
                .email("perintis@gmail.com")
                .password("amaterasu")
                .phoneNumber("+6289283839922")
                .dateOfBirth(Instant.now())
                .role(UserRoleEnum.ADMIN)
                .build();
        User userPerintisSaved = userRepository.save(perintis);

        String token = jwtService.generateToken(userPerintisSaved);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/cars")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<CarResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponsePaging<List<CarResponse>>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(2, response.getLastPage());
                    Assertions.assertEquals(20, response.getPerPage());
                    Assertions.assertEquals(31, response.getTotalItem());
                    Assertions.assertEquals(20, response.getData().size());

                });
    }

    @Test
    void getListCarAdminWithQueryNameSuccessTest() throws Exception {
        List<CarAuthorization> carAuthorizations = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            // Create car dummy data
            Car carData = Car.builder()
                    .name("Dummy Car " + i)
                    .imageUrl("testing_image")
                    .brand(CarBrandEnum.TOYOTA)
                    .year(2022)
                    .capacity(6)
                    .cc(2000)
                    .pricePerDay(200_000D)
                    .tax(0)
                    .discount(0)
                    .description("dummy car data")
                    .transmission(i % 2 == 0 ? CarTransmissionEnum.AT : CarTransmissionEnum.MT)
                    .tax(0)
                    .discount(0)
                    .build();
            Car savedCar = carRepository.save(carData);
            carAuthorizations.add(
                    CarAuthorization.builder()
                            .user(admin)
                            .car(savedCar)
                            .build()
            );
        }
        carAuthorizationRepository.saveAll(carAuthorizations.stream().toList());

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/cars")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .param("name", "dummy car 1")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<CarResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponsePaging<List<CarResponse>>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(1, response.getLastPage());
                    Assertions.assertEquals(20, response.getPerPage());
                    Assertions.assertEquals(11, response.getTotalItem());
                    Assertions.assertEquals(11, response.getData().size());

                });
    }

    @Test
    void getListCarAdminWithQueryTransmissionSuccessTest() throws Exception {
        List<CarAuthorization> carAuthorizations = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            // Create car dummy data
            Car carData = Car.builder()
                    .name("Dummy Car " + i)
                    .imageUrl("testing_image")
                    .brand(CarBrandEnum.TOYOTA)
                    .year(2022)
                    .capacity(6)
                    .cc(2000)
                    .pricePerDay(200_000D)
                    .tax(0)
                    .discount(0)
                    .description("dummy car data")
                    .transmission(i % 2 == 0 ? CarTransmissionEnum.AT : CarTransmissionEnum.MT)
                    .tax(0)
                    .discount(0)
                    .build();
            Car savedCar = carRepository.save(carData);
            carAuthorizations.add(
                    CarAuthorization.builder()
                            .user(admin)
                            .car(savedCar)
                            .build()
            );
        }
        carAuthorizationRepository.saveAll(carAuthorizations.stream().toList());

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/cars")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .param("transmission", "at")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<CarResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponsePaging<List<CarResponse>>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(1, response.getLastPage());
                    Assertions.assertEquals(20, response.getPerPage());
                    Assertions.assertEquals(15, response.getTotalItem());
                    Assertions.assertEquals(15, response.getData().size());

                });
    }

    @Test
    void getListCarUserSuccessTest() throws Exception {
        for (int i = 0; i < 30; i++) {
            // Create car dummy data
            Car carData = Car.builder()
                    .name("Dummy Car " + i)
                    .imageUrl("testing_image")
                    .brand(CarBrandEnum.TOYOTA)
                    .year(2022)
                    .capacity(6)
                    .cc(2000)
                    .pricePerDay(200_000D)
                    .tax(0)
                    .discount(0)
                    .description("dummy car data")
                    .transmission(i % 2 == 0 ? CarTransmissionEnum.AT : CarTransmissionEnum.MT)
                    .tax(0)
                    .discount(0)
                    .build();
            Car savedCar = carRepository.save(carData);
        }

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/cars")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .param("start_date_rent", "2024-04-20T10:00:00")
                                .param("duration", "1")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<CarResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponsePaging<List<CarResponse>>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(2, response.getLastPage());
                    Assertions.assertEquals(20, response.getPerPage());
                    Assertions.assertEquals(31, response.getTotalItem());
                    Assertions.assertEquals(20, response.getData().size());

                });
    }


    @Test
    void getListCarUserWithCarOrderedSuccessTest() throws Exception {
        for (int i = 0; i < 30; i++) {
            // Create car dummy data
            Car carData = Car.builder()
                    .name("Dummy Car " + i)
                    .imageUrl("testing_image")
                    .brand(CarBrandEnum.TOYOTA)
                    .year(2022)
                    .capacity(6)
                    .cc(2000)
                    .pricePerDay(200_000D)
                    .tax(0)
                    .discount(0)
                    .description("dummy car data")
                    .transmission(i % 2 == 0 ? CarTransmissionEnum.AT : CarTransmissionEnum.MT)
                    .tax(0)
                    .discount(0)
                    .build();
            Car savedCar = carRepository.save(carData);

            if (i % 2 == 0) {
                TransactionCreateRequest request = TransactionCreateRequest.builder()
                        .dateAndTime("2024-04-20T10:00:00")
                        .duration(1)
                        .carId(savedCar.getId().toString())
                        .userId(user.getId().toString())
                        .build();
                transactionService.createTransaction(user, request);
            }

        }

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/cars")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .param("start_date_rent", "2024-04-20T10:00:00")
                                .param("duration", "1")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isOk())
                .andExpectAll(result -> {
                    WebResponsePaging<List<CarResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponsePaging<List<CarResponse>>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
                    Assertions.assertEquals(1, response.getCurrentPage());
                    Assertions.assertEquals(1, response.getLastPage());
                    Assertions.assertEquals(20, response.getPerPage());
                    Assertions.assertEquals(16, response.getTotalItem());
                    Assertions.assertEquals(16, response.getData().size());

                });
    }

    @Test
    void getListCarUserEmptyDateErrorTest() throws Exception {

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/cars")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .param("duration", "1")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponsePaging<List<CarResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponsePaging<List<CarResponse>>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("start date must not be blank", response.getError());

                });
    }

    @Test
    void getListCarUserEmptyDurationErrorTest() throws Exception {

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/cars")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .param("start_date_rent", "2024-04-20T10:00:00")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(status().isBadRequest())
                .andExpectAll(result -> {
                    WebResponsePaging<List<CarResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponsePaging<List<CarResponse>>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
                    Assertions.assertEquals("duration must not be blank", response.getError());
                });
    }

    @Test
    void createCarAuthorizationSuccessTest() throws Exception {

        List<String> listCarId = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            // Create car dummy data
            Car carData = Car.builder()
                    .name("Dummy Car " + i)
                    .imageUrl("testing_image")
                    .brand(CarBrandEnum.TOYOTA)
                    .year(2022)
                    .capacity(6)
                    .cc(2000)
                    .pricePerDay(200_000D)
                    .tax(0)
                    .discount(0)
                    .description("dummy car data")
                    .transmission(i % 2 == 0 ? CarTransmissionEnum.AT : CarTransmissionEnum.MT)
                    .tax(0)
                    .discount(0)
                    .build();
            Car savedCar = carRepository.save(carData);
            listCarId.add(savedCar.getId().toString());
        }

        CarCreateAuthorizationRequest carCreateAuthorizationRequest = CarCreateAuthorizationRequest.builder()
                .carId(listCarId)
                .userId(List.of(admin.getId().toString()))
                .build();

        String token = jwtService.generateToken(admin);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/cars/authorization")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(carCreateAuthorizationRequest))
                )
                .andExpectAll(status().isCreated())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNull(response.getError());
                    Assertions.assertNotNull(response.getData());

                    Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatus());
                    Assertions.assertEquals("success add car authorization", response.getData());
                });
    }

    @Test
    void createCarAuthorizationUserAccessForbiddenErrorTest() throws Exception {

        List<String> listCarId = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            // Create car dummy data
            Car carData = Car.builder()
                    .name("Dummy Car " + i)
                    .imageUrl("testing_image")
                    .brand(CarBrandEnum.TOYOTA)
                    .year(2022)
                    .capacity(6)
                    .cc(2000)
                    .pricePerDay(200_000D)
                    .tax(0)
                    .discount(0)
                    .description("dummy car data")
                    .transmission(i % 2 == 0 ? CarTransmissionEnum.AT : CarTransmissionEnum.MT)
                    .tax(0)
                    .discount(0)
                    .build();
            Car savedCar = carRepository.save(carData);
            listCarId.add(savedCar.getId().toString());
        }

        CarCreateAuthorizationRequest carCreateAuthorizationRequest = CarCreateAuthorizationRequest.builder()
                .carId(listCarId)
                .userId(List.of(admin.getId().toString()))
                .build();

        String token = jwtService.generateToken(user);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/cars/authorization")
                                .header(AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(carCreateAuthorizationRequest))
                )
                .andExpectAll(status().isForbidden())
                .andExpectAll(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    Assertions.assertNotNull(response.getStatus());
                    Assertions.assertNotNull(response.getError());
                    Assertions.assertNull(response.getData());

                    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
                    Assertions.assertEquals("don't have a access", response.getError());
                });
    }

    /*@Test
    void testImage() throws IOException {
        boolean exists = Files.exists(Path.of("images/user/profile/1695719036091.jpg"));
        System.out.println(exists);

        boolean b = Files.deleteIfExists(Path.of("images/user/profile/1695719036091.jpg"));
        System.out.println("is delete: " + b);

    }*/

}
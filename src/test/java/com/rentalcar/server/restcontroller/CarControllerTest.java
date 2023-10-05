package com.rentalcar.server.restcontroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentalcar.server.entity.*;
import com.rentalcar.server.model.CarDetailResponse;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.repository.CarAuthorizationRepository;
import com.rentalcar.server.repository.CarRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.security.JwtService;
import com.rentalcar.server.service.AuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

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
    CarAuthorizationRepository carAuthorizationRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    AuthService authService;

    @Autowired
    ObjectMapper objectMapper;

    User admin;

    User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
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
    void testImage() throws IOException {
        boolean exists = Files.exists(Path.of("images/user/profile/1695719036091.jpg"));
        System.out.println(exists);

        boolean b = Files.deleteIfExists(Path.of("images/user/profile/1695719036091.jpg"));
        System.out.println("is delete: " + b);

    }
}
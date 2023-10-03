package com.rentalcar.server.service;

import com.rentalcar.server.entity.Car;
import com.rentalcar.server.entity.CarTransmissionEnum;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.CarDetailResponse;
import com.rentalcar.server.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    @Override
    public CarDetailResponse getDetailCar(User user, String id) {

        UUID carId;
        try {
            carId = UUID.fromString(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "car not found");
        }

        Car carData = carRepository.findById(carId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "car not found"));

        return CarDetailResponse.builder()
                .id(carData.getId().toString())
                .name(carData.getName())
                .image(carData.getImageUrl())
                .brand(carData.getBrand().name())
                .year(carData.getYear())
                .capacity(carData.getCapacity())
                .cc(carData.getCc())
                .pricePerDay(carData.getPricePerDay())
                .tax(carData.getTax())
                .discount(carData.getDiscount())
                .description(carData.getDescription())
                .transmission(carData.getTransmission().equals(CarTransmissionEnum.MT) ? "manual" : "automatic")
                .isActive(carData.getIsActive())
                .build();
    }
}

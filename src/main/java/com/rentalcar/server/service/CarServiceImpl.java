package com.rentalcar.server.service;

import com.rentalcar.server.entity.*;
import com.rentalcar.server.model.CarDetailResponse;
import com.rentalcar.server.repository.CarRepository;
import com.rentalcar.server.util.UUIDUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final UUIDUtils uuidUtils;

    @Override
    public CarDetailResponse getDetailCar(User user, String id) {

        UUID carId = uuidUtils.uuidFromString(id, "car not found");

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

    @Override
    public String deleteCarById(User user, String id) {
        if (user.getRole().equals(UserRoleEnum.USER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
        }

        UUID carId = uuidUtils.uuidFromString(id, "car not found");
        Car carData = carRepository.findById(carId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "car not found"));

        List<CarAuthorization> carAuthorizations = user.getCarAuthorizations().stream()
                .filter(carAuthorization -> carAuthorization.getId() != carData.getId())
                .toList();
        if (carAuthorizations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have authorization for this car");
        }

        carRepository.deleteById(carId);

        return "success delete car";
    }
}

package com.rentalcar.server.service;

import com.rentalcar.server.entity.*;
import com.rentalcar.server.model.CarCreateRequest;
import com.rentalcar.server.model.CarCreateResponse;
import com.rentalcar.server.model.CarDetailResponse;
import com.rentalcar.server.model.ImageDetailItem;
import com.rentalcar.server.repository.CarImageDetailRepository;
import com.rentalcar.server.repository.CarRepository;
import com.rentalcar.server.util.EnumUtils;
import com.rentalcar.server.util.UUIDUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    @Value("${image.car}")
    private String carPath;
    @Value("${image.car.detail}")
    private String carPathDetail;
    private final CarRepository carRepository;
    private final CarImageDetailRepository carImageDetailRepository;
    private final UUIDUtils uuidUtils;
    private final ValidationService validationService;
    private final FileStorageService fileStorageService;
    private final EnumUtils enumUtils;

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

    @Transactional
    @Override
    public CarCreateResponse createCar(User user, CarCreateRequest carCreateRequest, MultipartFile image, List<MultipartFile> imagesDetail) {
        if (user.getRole().equals(UserRoleEnum.USER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
        }

        validationService.validate(carCreateRequest);

        if (!Objects.nonNull(image) || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image must not be blank");
        }

        String imageCarPath = fileStorageService.storeFile(image, carPath);
        CarBrandEnum carBrandEnum = enumUtils.getCarBrandEnumFromString(carCreateRequest.getBrand());
        CarTransmissionEnum carTransmissionEnum = enumUtils.getCarTransmissionEnumFromString(carCreateRequest.getTransmission());
        Car car = Car.builder()
                .name(carCreateRequest.getName())
                .imageUrl(imageCarPath)
                .brand(carBrandEnum)
                .year(carCreateRequest.getYear())
                .capacity(carCreateRequest.getCapacity())
                .cc(carCreateRequest.getCc())
                .pricePerDay(carCreateRequest.getPrice())
                .tax(carCreateRequest.getTax())
                .discount(carCreateRequest.getDiscount())
                .description(carCreateRequest.getDescription())
                .transmission(carTransmissionEnum)
                .tax(carCreateRequest.getTax())
                .discount(carCreateRequest.getDiscount())
                .build();

        Car saveCar = carRepository.save(car);

        List<ImageDetailItem> imageDetailItems = new ArrayList<>();
        imagesDetail.forEach(multipartFile -> {
            String path = fileStorageService.storeFile(multipartFile, carPathDetail);
            CarImageDetail carImageDetail = CarImageDetail.builder()
                    .car(saveCar)
                    .imageUrl(path)
                    .build();
            CarImageDetail saveCarImageDetail = carImageDetailRepository.save(carImageDetail);
            imageDetailItems.add(
                    ImageDetailItem.builder()
                            .id(saveCarImageDetail.getId().toString())
                            .image(saveCar.getImageUrl())
                            .build()
            );
        });

        return CarCreateResponse.builder()
                .id(saveCar.getId().toString())
                .name(saveCar.getName())
                .cc(saveCar.getCc())
                .image(saveCar.getImageUrl())
                .price(saveCar.getPricePerDay())
                .isActive(saveCar.getIsActive())
                .year(saveCar.getYear())
                .description(saveCar.getDescription())
                .discount(saveCar.getDiscount())
                .tax(saveCar.getTax())
                .capacity(saveCar.getCapacity())
                .transmission(saveCar.getTransmission().name())
                .brand(saveCar.getBrand().name())
                .imageDetail(imageDetailItems.isEmpty() ? null : imageDetailItems)
                .build();
    }
}

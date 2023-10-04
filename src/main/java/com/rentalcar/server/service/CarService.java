package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CarService {

    CarDetailResponse getDetailCar(User user, String carId);

    String deleteCarById(User user, String carId);

    CarCreateResponse createCar(User user, CarCreateRequest carCreateRequest, MultipartFile image, List<MultipartFile> imagesDetail);

    String createCarAuthorization(User user, CarCreateAuthorizationRequest carCreateAuthorizationRequest);

}

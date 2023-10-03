package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.CarDetailResponse;

public interface CarService {

    CarDetailResponse getDetailCar(User user, String carId);

}

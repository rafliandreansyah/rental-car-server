package com.rentalcar.server.restcontroller;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.CarDetailResponse;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<CarDetailResponse>> getDetailCar(User user, @PathVariable("id") String id) {
        CarDetailResponse detailCarResponse = carService.getDetailCar(user, id);
        return ResponseEntity.ok(WebResponse.<CarDetailResponse>builder().status(HttpStatus.OK.value()).data(detailCarResponse).build());
    }

}

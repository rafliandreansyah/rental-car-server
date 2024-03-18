package com.rentalcar.server.restcontroller;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.*;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.model.base.WebResponsePaging;
import com.rentalcar.server.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<String>> deleteCarById(User user, @PathVariable("id") String id) {
        return ResponseEntity.ok(WebResponse.<String>builder().data(carService.deleteCarById(user, id)).status(HttpStatus.OK.value()).build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<CarCreateAndEditResponse>> createCar(
            User user,
            @ModelAttribute CarCreateRequest
                    carCreateRequest, @RequestParam(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "image_detail", required = false) List<MultipartFile> imagesDetail) {
        CarCreateAndEditResponse carResponse = carService.createCar(user, carCreateRequest, image, imagesDetail);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebResponse.<CarCreateAndEditResponse>builder().status(HttpStatus.CREATED.value()).data(carResponse).build());
    }

    @PostMapping(value = "/authorization", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<String>> createCarAuthorization(
            User user,
            @RequestBody CarCreateAuthorizationRequest carCreateAuthorizationRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(WebResponse.<String>builder().status(HttpStatus.CREATED.value()).data(carService.createCarAuthorization(user, carCreateAuthorizationRequest)).build());
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<CarCreateAndEditResponse>> editCar(
            User user,
            @ModelAttribute CarEditRequest carEditRequest,
            @PathVariable("id") String id,
            @RequestParam(name = "deleted_detail_images_id", required = false) List<String> imagesDetailId,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "image_detail", required = false) List<MultipartFile> imagesDetail
    ) {
        if (imagesDetailId != null && !imagesDetailId.isEmpty()) {
            carEditRequest.setDeletedDetailImagesId(imagesDetailId);
        }
        CarCreateAndEditResponse carCreateAndEditResponse = carService.editCar(user, id, carEditRequest, image, imagesDetail);
        return ResponseEntity.ok(WebResponse.<CarCreateAndEditResponse>builder().status(HttpStatus.OK.value()).data(carCreateAndEditResponse).build());
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponsePaging<List<CarResponse>>> getCars(
            User user,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "transmission", required = false) String transmission,
            @RequestParam(name = "start_date_rent", required = false) String startDateRent,
            @RequestParam(name = "duration", required = false) Integer duration,
            @RequestParam(name = "order_by_date_created", required = false) String order,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        CarRequest carRequest = CarRequest.builder()
                .name(name)
                .transmission(transmission)
                .duration(duration)
                .orderByDateCreated(order)
                .startDateRent(startDateRent)
                .page(page)
                .size(size)
                .build();

        Page<CarResponse> listCar = carService.getListCar(user, carRequest);
        return ResponseEntity.ok(
                WebResponsePaging.<List<CarResponse>>builder()
                        .totalItem(listCar.getTotalElements())
                        .perPage(listCar.getSize())
                        .currentPage(listCar.getNumber() + 1)
                        .lastPage(listCar.getTotalPages())
                        .status(HttpStatus.OK.value())
                        .data(listCar.getContent())
                        .build()
        );
    }

    @PostMapping(value = "/car/rating", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WebResponse<String>> createRating(
            User user,
            @RequestParam("car_id") String carId,
            @RequestParam("rating") Double rating,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(name = "image", required = false) MultipartFile image
            ) {

        String response = carService.createRating(user, carId, rating, comment, image);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WebResponse.<String>builder()
                        .status(HttpStatus.CREATED.value())
                        .data(response)
                        .build()
                );

    }


}

package com.rentalcar.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarCreateAuthorizationRequest {

    @NotBlank(message = "user id must not be blank")
    @JsonProperty("user_id")
    private List<String> userId;

    @NotBlank(message = "car id must not be blank")
    @JsonProperty("car_id")
    private List<String> carId;

}

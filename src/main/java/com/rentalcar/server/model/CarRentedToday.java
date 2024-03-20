package com.rentalcar.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarRentedToday {

    @JsonProperty("total_cars")
    private Integer totalCars;

    @JsonProperty("car_rented_today")
    private Integer carRentedToday;

}

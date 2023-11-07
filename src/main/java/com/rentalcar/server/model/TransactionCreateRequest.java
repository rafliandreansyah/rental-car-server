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
public class TransactionCreateRequest {

    @JsonProperty("date")
    private String dateAndTime;

    private Integer duration;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("car_id")
    private String carId;

}

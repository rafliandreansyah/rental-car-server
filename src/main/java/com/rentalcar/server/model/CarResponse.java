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
public class CarResponse {

    private String id;

    private String name;

    @JsonProperty("image_url")
    private String imageUrl;

    private Integer year;

    private String transmission;

    private Integer luggage;

    private Double rating;

    private Double price;

}

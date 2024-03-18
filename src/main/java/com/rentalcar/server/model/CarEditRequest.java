package com.rentalcar.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarEditRequest {

    private String name;

    private String brand;

    private Integer year;

    private Integer capacity;

    private Integer cc;

    private Double price;

    private String description;

    private String transmission;

    private Integer tax;

    private Integer discount;

    private Integer luggage;

    private List<String> deletedDetailImagesId;
}

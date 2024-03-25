package com.rentalcar.server.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarCreateRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "brand must not be blank")
    private String brand;

    @NotNull(message = "year must not be blank")
    private Integer year;

    @NotNull(message = "capacity must not be blank")
    private Integer capacity;

    @NotNull(message = "cc must not be blank")
    private Integer cc;

    @NotNull(message = "price must not be blank")
    private Double price;

    @NotBlank(message = "description must not be blank")
    private String description;

    @NotBlank(message = "transmission must not be blank")
    private String transmission;

    private Integer tax;

    private Integer discount;

    private Integer luggage;

}

package com.rentalcar.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarRequest {

    private String name;

    private String transmission;

    private String startDateRent;

    private Integer duration;

    private String orderByDateCreated;

    private Integer page;

    private Integer size;

}

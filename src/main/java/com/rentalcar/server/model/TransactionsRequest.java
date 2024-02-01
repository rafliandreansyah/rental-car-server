package com.rentalcar.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionsRequest {

    private String startDate;

    private String endDate;

    private String sort;

    private Integer page;

    private Integer size;



}

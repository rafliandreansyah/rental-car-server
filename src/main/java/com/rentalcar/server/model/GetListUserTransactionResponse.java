package com.rentalcar.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetListUserTransactionResponse {

    private String id;

    @JsonProperty("no_invoice")
    private String noInvoice;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    private Integer duration;

    @JsonProperty("car_name")
    private String carName;

    private String brand;

    @JsonProperty("total_price")
    private Double totalPrice;

    private String status;

}

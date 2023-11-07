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
public class TransactionCreateResponse {

    public String id;

    @JsonProperty("no_invoice")
    public String noInvoice;

    @JsonProperty("start_date")
    public String startDate;

    @JsonProperty("end_date")
    public String endDate;

    public Integer duration;

    @JsonProperty("car_name")
    public String carName;

    @JsonProperty("car_image_url")
    public String carImageUrl;

    public String brand;

    @JsonProperty("car_year")
    public Integer carYear;

    public Integer capacity;

    public Integer cc;

    public Double price;

    public Integer tax;

    public Integer discount;

    @JsonProperty("total_price")
    public Double totalPrice;

}
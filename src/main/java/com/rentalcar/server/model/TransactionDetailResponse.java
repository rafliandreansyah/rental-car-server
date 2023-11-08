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
public class TransactionDetailResponse {

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

    @JsonProperty("car_image_url")
    private String carImageUrl;

    private String brand;

    private Integer year;

    private Integer capacity;

    private Integer cc;

    private Double price;

    private Integer tax;

    private Integer discount;

    @JsonProperty("total_price")
    private Double totalPrice;

    private String status;

    @JsonProperty("user_id")
    private String userId;

    private TransactionDetailUserDataResponse user;

    @JsonProperty("car_id")
    private String carId;

    @JsonProperty("user_approved")
    private String userApproved;

    @JsonProperty("payment_image")
    private String paymentImage;

    @JsonProperty("created_at")
    private String createdAt;

}
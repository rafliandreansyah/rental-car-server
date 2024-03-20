package com.rentalcar.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionEditResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("paymentImage")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String paymentImage;

}

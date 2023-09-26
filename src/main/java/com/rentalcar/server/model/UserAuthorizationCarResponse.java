package com.rentalcar.server.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthorizationCarResponse {

    private String id;

    @JsonProperty("image_url")
    private String imageUrl;

    private String name;

    private String email;

    private String phone;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("cars_authorizations")
    private List<CarResponse> carsAuthorizations;

}

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
public class UserCreateResponse {

    private String id;

    private String email;

    private String name;

    @JsonProperty("image_url")
    private String imageUrl;

    private String dob;

    private String phone;

    private String role;

    @JsonProperty("is_active")
    private Boolean isActive;

}

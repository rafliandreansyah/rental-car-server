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
public class GetDetailUserResponse {

    private String id;

    private String name;

    private String email;

    @JsonProperty("image_url")
    private String imageUrl;

    private String dob;

    private String phone;

    @JsonProperty("is_active")
    private boolean isActive;

    private String role;

    @JsonProperty("date_created")
    private String dateCreated;

}

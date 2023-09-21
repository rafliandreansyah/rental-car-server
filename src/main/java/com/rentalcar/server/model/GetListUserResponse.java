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
public class GetListUserResponse {

    private String id;

    @JsonProperty("image_url")
    private String imageUrl;

    private String name;

    private String phone;

    @JsonProperty("is_active")
    private Boolean isActive;

}

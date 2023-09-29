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
public class UserEditResponse {

    private String id;
    private String name;
    private String image;
    private String dob;
    @JsonProperty("is_active")
    private Boolean isActive;
    private String role;

}

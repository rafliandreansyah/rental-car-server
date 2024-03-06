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
public class ResetNewPasswordRequest {

    private String token;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("new_password")
    private String newPassword;

}

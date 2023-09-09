package com.rentalcar.server.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticateRequest {

    @NotBlank(message = "email must be not blank")
    @Email(message = "format email is not valid")
    private String email;

    @NotBlank(message = "password must not be blank")
    private String password;

}

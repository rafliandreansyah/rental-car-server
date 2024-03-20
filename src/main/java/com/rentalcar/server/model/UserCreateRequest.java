package com.rentalcar.server.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "email must not be blank")
    @Email(message = "format email is not valid")
    private String email;

    @NotBlank(message = "password must not be blank")
    @Size(min = 8, message = "password must have 8 characters or more")
    private String password;

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "phone must not be blank")
    @Pattern(regexp = "^(\\+62)[0-9]{9,12}$", message = "phone number is not valid")
    private String phone;

    @NotBlank(message = "role must not be blank")
    private String role;

    private String dob;

}

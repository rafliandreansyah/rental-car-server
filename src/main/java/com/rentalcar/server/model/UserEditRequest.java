package com.rentalcar.server.model;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEditRequest {

    private String name;
    @Pattern(regexp = "^(\\+62)[0-9]{9,12}$", message = "phone number is not valid")
    private String phone;
    private String dob;
    private Boolean isActive;
    private String role;

}

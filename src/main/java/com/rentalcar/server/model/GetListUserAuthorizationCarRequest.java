package com.rentalcar.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetListUserAuthorizationCarRequest {

    private String name;

    private String email;

    private Boolean isActive;

    private Integer page;

    private Integer size;

}

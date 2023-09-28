package com.rentalcar.server.util;

import com.rentalcar.server.entity.UserRoleEnum;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EnumUtils {
    public UserRoleEnum getUserRoleEnumFromString(String role) {
        UserRoleEnum userRoleEnum;

        if (role.equalsIgnoreCase(UserRoleEnum.USER.name())) {
            userRoleEnum = UserRoleEnum.USER;
        } else if (role.equalsIgnoreCase(UserRoleEnum.ADMIN.name())) {
            userRoleEnum = UserRoleEnum.ADMIN;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user role not found");
        }
        return userRoleEnum;
    }
}

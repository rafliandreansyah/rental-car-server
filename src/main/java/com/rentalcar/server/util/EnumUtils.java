package com.rentalcar.server.util;

import com.rentalcar.server.entity.CarBrandEnum;
import com.rentalcar.server.entity.CarTransmissionEnum;
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

    public CarTransmissionEnum getCarTransmissionEnumFromString(String transmission) {
        CarTransmissionEnum carTransmissionEnum;

        if (transmission.equalsIgnoreCase(CarTransmissionEnum.AT.name())) {
            carTransmissionEnum = CarTransmissionEnum.AT;
        } else if (transmission.equalsIgnoreCase(CarTransmissionEnum.MT.name())) {
            carTransmissionEnum = CarTransmissionEnum.MT;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "car transmission not found");
        }

        return carTransmissionEnum;
    }

    public CarBrandEnum getCarBrandEnumFromString(String brand) {
        CarBrandEnum carBrandEnum;

        if (brand.equalsIgnoreCase(CarBrandEnum.TOYOTA.name())) {
            carBrandEnum = CarBrandEnum.TOYOTA;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.HONDA.name())) {
            carBrandEnum = CarBrandEnum.HONDA;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.DAIHATSU.name())) {
            carBrandEnum = CarBrandEnum.DAIHATSU;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.WULING.name())) {
            carBrandEnum = CarBrandEnum.WULING;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.SUZUKI.name())) {
            carBrandEnum = CarBrandEnum.SUZUKI;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.MITSUBISHI.name())) {
            carBrandEnum = CarBrandEnum.MITSUBISHI;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.NISSAN.name())) {
            carBrandEnum = CarBrandEnum.NISSAN;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.MAZDA.name())) {
            carBrandEnum = CarBrandEnum.MAZDA;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.SUBARU.name())) {
            carBrandEnum = CarBrandEnum.SUBARU;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.ISUZU.name())) {
            carBrandEnum = CarBrandEnum.ISUZU;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.FORD.name())) {
            carBrandEnum = CarBrandEnum.FORD;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.CHEVROLET.name())) {
            carBrandEnum = CarBrandEnum.CHEVROLET;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.HYUNDAI.name())) {
            carBrandEnum = CarBrandEnum.HYUNDAI;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.KIA.name())) {
            carBrandEnum = CarBrandEnum.KIA;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.MERCEDES_BENZ.name())) {
            carBrandEnum = CarBrandEnum.MERCEDES_BENZ;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.BMW.name())) {
            carBrandEnum = CarBrandEnum.BMW;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.AUDI.name())) {
            carBrandEnum = CarBrandEnum.AUDI;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.VOLKSWAGEN.name())) {
            carBrandEnum = CarBrandEnum.VOLKSWAGEN;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.PEUGEOT.name())) {
            carBrandEnum = CarBrandEnum.PEUGEOT;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.RENAULT.name())) {
            carBrandEnum = CarBrandEnum.RENAULT;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.VOLVO.name())) {
            carBrandEnum = CarBrandEnum.VOLVO;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.CHERY.name())) {
            carBrandEnum = CarBrandEnum.CHERY;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.ESEMKA.name())) {
            carBrandEnum = CarBrandEnum.ESEMKA;
        } else if (brand.equalsIgnoreCase(CarBrandEnum.LEXUS.name())) {
            carBrandEnum = CarBrandEnum.LEXUS;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "car brand not found");
        }

        return carBrandEnum;
    }
}

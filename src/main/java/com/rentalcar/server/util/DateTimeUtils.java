package com.rentalcar.server.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@Service
public class DateTimeUtils {

    public LocalDateTime localDateTimeFromInstantZoneJakarta(Instant instant) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Jakarta"));
        return localDateTime.withNano(0);
    }

    public LocalDate localDateFromInstantZoneJakarta(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.of("Asia/Jakarta"));
    }

    public LocalDateTime localDateTimeFromString(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "format date invalid");
        }
    }

    public Instant instantFromLocalDateTimeZoneJakarta(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.of("Asia/Jakarta")).toInstant();
    }

}

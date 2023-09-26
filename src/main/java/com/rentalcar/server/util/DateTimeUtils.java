package com.rentalcar.server.util;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class DateTimeUtils {

    public LocalDateTime localDateTimeFromInstantZoneJakarta(Instant instant) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Jakarta"));
        return localDateTime.withNano(0);
    }

    public LocalDate localDateFromInstantZoneJakarta(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.of("Asia/Jakarta"));
    }

}

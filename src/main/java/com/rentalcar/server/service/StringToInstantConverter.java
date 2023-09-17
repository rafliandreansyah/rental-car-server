package com.rentalcar.server.service;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class StringToInstantConverter implements Converter<String, Instant> {
    @Override
    public Instant convert(String source) {
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.parse(source), ZoneId.of("Asia/Jakarta"));
            return zonedDateTime.toInstant();
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}

package com.rentalcar.server.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UUIDUtils {

    public UUID uuidFromString(String stringId, String error) {
        UUID id;
        try {
            id = UUID.fromString(stringId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, error);
        }
        return id;
    }

}

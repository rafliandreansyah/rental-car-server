package com.rentalcar.server.service;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService{

    private final Validator validator;

    @Override
    public void validate(Object data) {
        Set<ConstraintViolation<Object>> validate = validator.validate(data);
        if (validate.size() > 0) {
            throw new ConstraintViolationException(validate);
        }
    }
}

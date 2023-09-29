package com.rentalcar.server.handler;

import com.rentalcar.server.model.base.WebResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler {


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<WebResponse<String>> constraintViolationHandler(ConstraintViolationException constraintViolationException) {
        var errorMessage = "";
        try {
            if (constraintViolationException.getMessage().contains(",")) {
                String messageIndex0 = constraintViolationException.getMessage().split(",")[0];
                if (constraintViolationException.getMessage().contains(":")){
                    errorMessage = messageIndex0.split(":")[1].trim();
                } else {
                    errorMessage = messageIndex0;
                }
            } else {
                if (constraintViolationException.getMessage().contains(":")){
                    errorMessage = constraintViolationException.getMessage().split(":")[1].trim();
                } else {
                    errorMessage = constraintViolationException.getMessage();
                }
            }
        }catch (Exception e) {
            errorMessage = constraintViolationException.getMessage();
        }

        return ResponseEntity.badRequest().body(WebResponse.<String>builder().error(errorMessage).status(HttpStatus.BAD_REQUEST.value()).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<WebResponse<String>> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException methodArgumentNotValidException) {
        List<String> errors = new ArrayList<>();
        methodArgumentNotValidException.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);

        });
        return ResponseEntity.badRequest().body(WebResponse.<String>builder().error(errors.get(0)).status(HttpStatus.BAD_REQUEST.value()).build());

    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<WebResponse<String>> responseStatusExceptionHandler(ResponseStatusException responseStatusException) {
        return new ResponseEntity<>(WebResponse.<String>builder().error(responseStatusException.getReason()).status(responseStatusException.getStatusCode().value()).build(), responseStatusException.getStatusCode());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<WebResponse<String>> nullPointerExceptionHandler(NullPointerException nullPointerException) {
        nullPointerException.printStackTrace();
        return new ResponseEntity<>(WebResponse.<String>builder().error("internal server error").status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<WebResponse<String>> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
        methodArgumentTypeMismatchException.printStackTrace();
        return new ResponseEntity<>(WebResponse.<String>builder().error("invalid request data type").status(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
    }

}

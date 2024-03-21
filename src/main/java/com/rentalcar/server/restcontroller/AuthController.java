package com.rentalcar.server.restcontroller;

import com.rentalcar.server.model.*;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<String>> register(@RequestBody RegisterRequest request) {
        String register = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebResponse.<String>builder().data(register).status(HttpStatus.CREATED.value()).build());
    }

    @PostMapping(value = "/authenticate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<AuthenticateResponse>> authenticate(@RequestBody AuthenticateRequest request) {
        var authenticate = authService.authenticate(request);
        return ResponseEntity.ok(WebResponse.<AuthenticateResponse>builder().data(authenticate).status(HttpStatus.OK.value()).build());
    }

    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<String>> requestResetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        String resetRes = authService.requestResetPassword(resetPasswordRequest, request);
        return ResponseEntity.ok(
                WebResponse.<String>builder()
                        .status(HttpStatus.OK.value())
                        .data(resetRes)
                        .build()
        );
    }

    @GetMapping(value = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<ResetPasswordResponse>> getResetPassword(
            @RequestParam("token") String token
    ) {
        ResetPasswordResponse resetTokenByToken = authService.getResetTokenByToken(token);
        return ResponseEntity.ok(WebResponse.<ResetPasswordResponse>builder()
                .status(HttpStatus.OK.value())
                .data(resetTokenByToken)
                .build()
        );
    }

    @PostMapping(value = "/reset-new-password")
    public ResponseEntity<WebResponse<String>> resetNewPassword(
            @RequestBody ResetNewPasswordRequest resetNewPasswordRequest
    ) {
        String response = authService.resetNewPassword(resetNewPasswordRequest);
        return ResponseEntity.ok(WebResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .data(response)
                .build());
    }

}

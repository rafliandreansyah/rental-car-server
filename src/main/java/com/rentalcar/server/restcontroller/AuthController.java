package com.rentalcar.server.restcontroller;

import com.rentalcar.server.model.AuthenticateRequest;
import com.rentalcar.server.model.AuthenticateResponse;
import com.rentalcar.server.model.RegisterRequest;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<String>> register(@RequestBody RegisterRequest request){
        String register = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebResponse.<String>builder().data(register).status(HttpStatus.CREATED.value()).build());
    }

    @PostMapping(value = "/authenticate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<AuthenticateResponse>> authenticate(@RequestBody AuthenticateRequest request) {
        var authenticate = authService.authenticate(request);
        return ResponseEntity.ok(WebResponse.<AuthenticateResponse>builder().data(authenticate).build());
    }

}

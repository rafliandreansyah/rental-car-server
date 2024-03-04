package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.AuthenticateRequest;
import com.rentalcar.server.model.AuthenticateResponse;
import com.rentalcar.server.model.RegisterRequest;

public interface AuthService {

    String register(RegisterRequest request);

    User createAdmin(User user);

    User createUser(User user);

    AuthenticateResponse authenticate(AuthenticateRequest authenticateRequest);

}

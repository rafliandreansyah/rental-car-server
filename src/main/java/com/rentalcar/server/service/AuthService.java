package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.RegisterRequest;

public interface AuthService {

    String register(RegisterRequest request);

    User createAdmin(User user);

}

package com.rentalcar.server.service;

import com.rentalcar.server.model.CreateUserRequest;
import com.rentalcar.server.model.CreateUserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    CreateUserResponse createUser(CreateUserRequest request, MultipartFile file);

}

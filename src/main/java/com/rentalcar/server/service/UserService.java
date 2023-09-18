package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.CreateUserRequest;
import com.rentalcar.server.model.CreateUserResponse;
import com.rentalcar.server.model.GetDetailUserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    CreateUserResponse createUser(CreateUserRequest request, MultipartFile file);

    GetDetailUserResponse getDetailUser(User user, String userId);

}

package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserCreateResponse createUser(UserCreateRequest request, MultipartFile file);

    DetailUserResponse getDetailUser(User user, String userId);

    String deleteUserById(User user, String userId);
    Page<UserResponse> getListUser(User user, UserRequest getListUserRequest);

    Page<UserTransactionResponse> getListUserTransaction(User user, UserTransactionRequest userTransactionRequest);

    Page<UserAuthorizationCarResponse> getListUserAuthorizationCar(User user, UserAuthorizationCarRequest getListUserAuthorizationCarRequest);

    UserEditResponse editUser(User user, String userId, UserEditRequest userEditRequest, MultipartFile file);

}

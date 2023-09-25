package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    CreateUserResponse createUser(CreateUserRequest request, MultipartFile file);

    GetDetailUserResponse getDetailUser(User user, String userId);

    String deleteUserById(User user, String userId);
    Page<GetListUserResponse> getListUser(User user, GetListUserRequest getListUserRequest);

    Page<GetListUserTransactionResponse> getListUserTransaction(User user, GetListUserTransactionRequest getListUserTransactionRequest);

}

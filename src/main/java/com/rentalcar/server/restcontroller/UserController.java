package com.rentalcar.server.restcontroller;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.*;
import com.rentalcar.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<CreateUserResponse>> createUser(@ModelAttribute @Valid CreateUserRequest request, @RequestParam("image") MultipartFile file) {
        CreateUserResponse response = userService.createUser(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebResponse.<CreateUserResponse>builder().data(response).status(HttpStatus.OK.value()).build());

    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<GetDetailUserResponse>> getDetailUser(User user, @PathVariable("id") String userId) {
        GetDetailUserResponse detailUser = userService.getDetailUser(user, userId);
        return ResponseEntity.ok(WebResponse.<GetDetailUserResponse>builder().status(HttpStatus.OK.value()).data(detailUser).build());
    }

    @DeleteMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<String>> deleteUserById(User user, @PathVariable("id") String userId) {
        String deleteUserMessage = userService.deleteUserById(user, userId);
        return ResponseEntity.ok(WebResponse.<String>builder().data(deleteUserMessage).status(HttpStatus.OK.value()).build());
    }


    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponsePaging<List<GetListUserResponse>>> getUsers(
            User user,
            @RequestParam(name = "role", required = false, defaultValue = "USER") String role,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "is_active", required = false, defaultValue = "true") Boolean isActive,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {

        GetListUserRequest getListRequest = GetListUserRequest.builder()
                .email(email)
                .name(name)
                .role(role)
                .isActive(isActive)
                .page(page)
                .size(size)
                .build();

        Page<GetListUserResponse> listUser = userService.getListUser(user, getListRequest);

        return ResponseEntity.ok(WebResponsePaging.<List<GetListUserResponse>>builder()
                .totalItem(listUser.getTotalElements())
                .perPage(listUser.getSize())
                .currentPage(listUser.getNumber() + 1)
                .lastPage(listUser.getTotalPages())
                .status(HttpStatus.OK.value())
                .data(listUser.getContent())
                .build());

    }

    @GetMapping(
            value = "/transactions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponsePaging<List<GetListUserTransactionResponse>>> getListUserTransactions(
            User user,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {

        Page<GetListUserTransactionResponse> listUserTransaction = userService.getListUserTransaction(user, GetListUserTransactionRequest.builder().page(page).size(size).build());
        return ResponseEntity.ok(WebResponsePaging.<List<GetListUserTransactionResponse>>builder()
                .totalItem(listUserTransaction.getTotalElements())
                .perPage(listUserTransaction.getSize())
                .currentPage(listUserTransaction.getNumber() + 1)
                .lastPage(listUserTransaction.getTotalPages())
                .status(HttpStatus.OK.value())
                .data(listUserTransaction.getContent())
                .build());
    }

}

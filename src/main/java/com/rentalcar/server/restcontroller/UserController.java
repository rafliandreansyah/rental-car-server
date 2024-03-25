package com.rentalcar.server.restcontroller;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.*;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.model.base.WebResponsePaging;
import com.rentalcar.server.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "User")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<UserCreateResponse>> createUser(@ModelAttribute UserCreateRequest request, @RequestParam(value = "image", required = false) MultipartFile file) {
        UserCreateResponse response = userService.createUser(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebResponse.<UserCreateResponse>builder().data(response).status(HttpStatus.OK.value()).build());

    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<DetailUserResponse>> getDetailUser(@Parameter(hidden = true) User user, @PathVariable("id") String userId) {
        DetailUserResponse detailUser = userService.getDetailUser(user, userId);
        return ResponseEntity.ok(WebResponse.<DetailUserResponse>builder().status(HttpStatus.OK.value()).data(detailUser).build());
    }

    @DeleteMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<String>> deleteUserById(@Parameter(hidden = true) User user, @PathVariable("id") String userId) {
        String deleteUserMessage = userService.deleteUserById(user, userId);
        return ResponseEntity.ok(WebResponse.<String>builder().data(deleteUserMessage).status(HttpStatus.OK.value()).build());
    }


    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponsePaging<List<UserResponse>>> getUsers(
            @Parameter(hidden = true) User user,
            @RequestParam(name = "role", required = false, defaultValue = "USER") String role,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "is_active", required = false, defaultValue = "true") Boolean isActive,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {

        UserRequest getListRequest = UserRequest.builder()
                .email(email)
                .name(name)
                .role(role)
                .isActive(isActive)
                .page(page)
                .size(size)
                .build();

        Page<UserResponse> listUser = userService.getListUser(user, getListRequest);

        return ResponseEntity.ok(WebResponsePaging.<List<UserResponse>>builder()
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
    public ResponseEntity<WebResponsePaging<List<UserTransactionResponse>>> getListUserTransactions(
            @Parameter(hidden = true) User user,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {

        Page<UserTransactionResponse> listUserTransaction = userService.getListUserTransaction(user, UserTransactionRequest.builder().page(page).size(size).build());
        return ResponseEntity.ok(WebResponsePaging.<List<UserTransactionResponse>>builder()
                .totalItem(listUserTransaction.getTotalElements())
                .perPage(listUserTransaction.getSize())
                .currentPage(listUserTransaction.getNumber() + 1)
                .lastPage(listUserTransaction.getTotalPages())
                .status(HttpStatus.OK.value())
                .data(listUserTransaction.getContent())
                .build());
    }

    @GetMapping(
            value = "/authorization",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponsePaging<List<UserAuthorizationCarResponse>>> getListUserCarAuthorization(
            @Parameter(hidden = true) User user,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "is_active", required = false, defaultValue = "true") Boolean isActive,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        UserAuthorizationCarRequest listUserAuthorizationCarRequest = UserAuthorizationCarRequest.builder()
                .email(email)
                .name(name)
                .isActive(isActive)
                .page(page)
                .size(size)
                .build();

        Page<UserAuthorizationCarResponse> listUserAuthorizationCar = userService.getListUserAuthorizationCar(user, listUserAuthorizationCarRequest);
        return ResponseEntity.ok(WebResponsePaging.<List<UserAuthorizationCarResponse>>builder()
                .totalItem(listUserAuthorizationCar.getTotalElements())
                .perPage(listUserAuthorizationCar.getSize())
                .currentPage(listUserAuthorizationCar.getNumber() + 1)
                .lastPage(listUserAuthorizationCar.getTotalPages())
                .status(HttpStatus.OK.value())
                .data(listUserAuthorizationCar.getContent())
                .build());
    }

    @PatchMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<UserEditResponse>> editUser(
            @Parameter(hidden = true) User user,
            @PathVariable("id") String userId,
            @ModelAttribute @Valid UserEditRequest userEditRequest,
            @RequestParam(value = "is_active", required = false) Boolean isActive,
            @RequestParam(value = "image", required = false) MultipartFile multipartFile
    ) {
        if (isActive != null) {
            userEditRequest.setIsActive(isActive);
        }
        UserEditResponse userEditResponse = userService.editUser(user, userId, userEditRequest, multipartFile);
        return ResponseEntity.ok(WebResponse.<UserEditResponse>builder().status(HttpStatus.OK.value()).data(userEditResponse).build());
    }

}

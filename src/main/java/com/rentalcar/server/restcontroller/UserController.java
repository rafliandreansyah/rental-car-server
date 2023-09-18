package com.rentalcar.server.restcontroller;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.CreateUserRequest;
import com.rentalcar.server.model.CreateUserResponse;
import com.rentalcar.server.model.GetDetailUserResponse;
import com.rentalcar.server.model.WebResponse;
import com.rentalcar.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

}

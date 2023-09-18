package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.CreateUserRequest;
import com.rentalcar.server.model.CreateUserResponse;
import com.rentalcar.server.model.GetDetailUserResponse;
import com.rentalcar.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("${image.user.profile}")
    private String userPath;
    private final FileStorageService fileStorageService;
    private final ValidationService validationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public CreateUserResponse createUser(CreateUserRequest request, MultipartFile file) {

        Optional<User> userByEmail = userRepository.findByEmail(request.getEmail().trim());
        if (userByEmail.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already register");
        }
        Optional<User> userByPhoneNumber = userRepository.findByPhoneNumber(request.getPhone().trim());
        if (userByPhoneNumber.isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "phone number already use");
        }

        String pathImage = null;
        if (!file.isEmpty()){
            pathImage = fileStorageService.storeFile(file, userPath);
        }

        if (!request.getRole().equalsIgnoreCase("admin") && !request.getRole().equalsIgnoreCase("user")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user role is not found");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhone())
                .role(request.getRole().trim().equalsIgnoreCase("admin") ? UserRoleEnum.ADMIN : UserRoleEnum.USER)
                .imageUrl(pathImage)
                .dateOfBirth(request.getDob())
                .build();

        User saveUserData = userRepository.save(user);

        return CreateUserResponse.builder()
                .id(saveUserData.getId().toString())
                .name(saveUserData.getName())
                .email(saveUserData.getEmail())
                .imageUrl(saveUserData.getImageUrl())
                .phone(saveUserData.getPhoneNumber())
                .dob(LocalDateTime.ofInstant(saveUserData.getDateOfBirth(), ZoneId.of("Asia/Jakarta")).toString())
                .role(saveUserData.getRole().name().trim().toLowerCase())
                .isActive(saveUserData.getIsActive())
                .build();
    }

    @Override
    public GetDetailUserResponse getDetailUser(User user, String userId) {

        if (!Objects.equals(user.getId().toString(), userId.trim())) {
            if (user.getRole() != null && user.getRole().equals(UserRoleEnum.USER)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
            }
        }

        UUID idUser;
        try {
            idUser = UUID.fromString(userId);
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }

        User userLoadedDB = userRepository.findById(idUser).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        return GetDetailUserResponse.builder()
                .id(userLoadedDB.getId().toString())
                .name(userLoadedDB.getName())
                .email(userLoadedDB.getEmail())
                .imageUrl(userLoadedDB.getImageUrl())
                .dob(LocalDateTime.ofInstant(userLoadedDB.getDateOfBirth(), ZoneId.of("Asia/Jakarta")).toString())
                .phone(userLoadedDB.getPhoneNumber())
                .isActive(userLoadedDB.getIsActive())
                .role(userLoadedDB.getRole().equals(UserRoleEnum.ADMIN) ? userLoadedDB.getRole().name() : null)
                .dateCreated(LocalDateTime.ofInstant(userLoadedDB.getCreatedAt(), ZoneId.of("Asia/Jakarta")).toString())
                .build();
    }
}

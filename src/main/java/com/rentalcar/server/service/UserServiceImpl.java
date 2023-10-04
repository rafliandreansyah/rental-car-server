package com.rentalcar.server.service;

import com.rentalcar.server.entity.Transaction;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.*;
import com.rentalcar.server.repository.TransactionRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.util.DateTimeUtils;
import com.rentalcar.server.util.EnumUtils;
import com.rentalcar.server.util.UUIDUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("${image.user.profile}")
    private String userPath;
    private final FileStorageService fileStorageService;
    private final ValidationService validationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionRepository transactionRepository;
    private final DateTimeUtils dateTimeUtils;
    private final EnumUtils enumUtils;
    private final UUIDUtils uuidUtils;

    @Transactional
    @Override
    public UserCreateResponse createUser(UserCreateRequest request, MultipartFile file) {
        validationService.validate(request);
        Optional<User> userByEmail = userRepository.findByEmail(request.getEmail().trim());
        if (userByEmail.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already register");
        }
        Optional<User> userByPhoneNumber = userRepository.findByPhoneNumber(request.getPhone().trim());
        if (userByPhoneNumber.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "phone number already use");
        }
        System.out.println("File " + file);
        String pathImage = null;
        if (Objects.nonNull(file) && !file.isEmpty()) {
            pathImage = fileStorageService.storeFile(file, userPath);
        }

        if (!request.getRole().equalsIgnoreCase("admin") && !request.getRole().equalsIgnoreCase("user")) {
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

        return UserCreateResponse.builder()
                .id(saveUserData.getId().toString())
                .name(saveUserData.getName())
                .email(saveUserData.getEmail())
                .imageUrl(saveUserData.getImageUrl())
                .phone(saveUserData.getPhoneNumber())
                .dob(dateTimeUtils.localDateFromInstantZoneJakarta(saveUserData.getDateOfBirth()).toString())
                .role(saveUserData.getRole().name().trim().toLowerCase())
                .isActive(saveUserData.getIsActive())
                .build();
    }

    @Override
    public DetailUserResponse getDetailUser(User user, String userId) {

        if (!Objects.equals(user.getId().toString(), userId.trim())) {
            if (user.getRole() != null && user.getRole().equals(UserRoleEnum.USER)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
            }
        }

        UUID idUser = uuidUtils.uuidFromString(userId, "user not found");

        User userLoadedDB = userRepository.findById(idUser).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        return DetailUserResponse.builder()
                .id(userLoadedDB.getId().toString())
                .name(userLoadedDB.getName())
                .email(userLoadedDB.getEmail())
                .imageUrl(userLoadedDB.getImageUrl())
                .dob(dateTimeUtils.localDateFromInstantZoneJakarta(userLoadedDB.getDateOfBirth()).toString())
                .phone(userLoadedDB.getPhoneNumber())
                .isActive(userLoadedDB.getIsActive())
                .role(userLoadedDB.getRole().equals(UserRoleEnum.ADMIN) ? userLoadedDB.getRole().name() : null)
                .dateCreated(dateTimeUtils.localDateTimeFromInstantZoneJakarta(userLoadedDB.getCreatedAt()).toString())
                .build();
    }

    @Override
    public String deleteUserById(User user, String userId) {
        if (user.getRole().equals(UserRoleEnum.USER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
        }

        UUID idUser = uuidUtils.uuidFromString(userId, "user not found");

        userRepository.findById(idUser).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        userRepository.deleteById(idUser);

        return "success delete data user";

    }

    public Page<UserResponse> getListUser(User user, UserRequest getListUserRequest) {

        getListUserRequest.setPage(getListUserRequest.getPage() > 0 ? getListUserRequest.getPage() - 1 : getListUserRequest.getPage());

        if (user.getRole().equals(UserRoleEnum.USER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
        }

        Specification<User> specification = (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    criteriaBuilder.notEqual(
                            root.get("id"), user.getId()
                    )
            );

            if (Objects.nonNull(getListUserRequest.getName())) {
                predicates.add(
                        criteriaBuilder.like(
                                root.get("name"), "%" + getListUserRequest.getName() + "%"
                        )
                );
            }

            if (Objects.nonNull(getListUserRequest.getEmail())) {
                predicates.add(
                        criteriaBuilder.like(
                                root.get("email"), "%" + getListUserRequest.getEmail() + "%"
                        )
                );
            }

            if (Objects.nonNull(getListUserRequest.getRole())) {
                UserRoleEnum userRoleEnum = enumUtils.getUserRoleEnumFromString(getListUserRequest.getRole());
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("role"), userRoleEnum
                        )
                );
            }

            if (Objects.nonNull(getListUserRequest.getIsActive())) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("isActive"), getListUserRequest.getIsActive()
                        )
                );
            }

            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };

        Pageable pageable = PageRequest.of(getListUserRequest.getPage(), getListUserRequest.getSize(), Sort.by(Sort.Order.desc("createdAt")));
        Page<User> pagingUsers = userRepository.findAll(specification, pageable);

        List<UserResponse> responses = pagingUsers.stream()
                .map(userData -> UserResponse
                        .builder()
                        .id(userData.getId().toString())
                        .email(userData.getEmail())
                        .name(userData.getName())
                        .imageUrl(userData.getImageUrl())
                        .isActive(userData.getIsActive())
                        .build()
                ).toList();
        return new PageImpl<>(responses, pageable, pagingUsers.getTotalElements());
    }

    @Override
    public Page<UserTransactionResponse> getListUserTransaction(User user, UserTransactionRequest userTransactionRequest) {

        userTransactionRequest.setPage(userTransactionRequest.getPage() > 0 ? userTransactionRequest.getPage() - 1 : userTransactionRequest.getPage());

        Pageable pageable = PageRequest.of(userTransactionRequest.getPage(), userTransactionRequest.getSize(), Sort.by(Sort.Order.desc("createdAt")));
        Page<Transaction> transactionsByUserId = transactionRepository.findAllByUserId(user.getId(), pageable);

        List<UserTransactionResponse> responses = transactionsByUserId.stream()
                .map(transaction -> UserTransactionResponse
                        .builder()
                        .id(transaction.getId().toString())
                        .startDate(dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toString())
                        .endDate(dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getEndDate()).toString())
                        .duration(transaction.getDurationDay())
                        .noInvoice(transaction.getNoInvoice())
                        .status(transaction.getStatus().name())
                        .brand(transaction.getCarBrand().name().toLowerCase())
                        .carName(transaction.getCarName())
                        .totalPrice(transaction.getTotalPrice())
                        .build()
                )
                .toList();

        return new PageImpl<>(responses, pageable, transactionsByUserId.getTotalElements());
    }

    @Override
    public Page<UserAuthorizationCarResponse> getListUserAuthorizationCar(User user, UserAuthorizationCarRequest getListUserAuthorizationCarRequest) {

        getListUserAuthorizationCarRequest.setPage(getListUserAuthorizationCarRequest.getPage() > 0 ? getListUserAuthorizationCarRequest.getPage() - 1 : getListUserAuthorizationCarRequest.getPage());

        if (user.getRole().equals(UserRoleEnum.USER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
        }

        Specification<User> specification = ((root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    criteriaBuilder.notEqual(
                            root.get("id"), user.getId()
                    )
            );

            predicates.add(
                    criteriaBuilder.isNotEmpty(root.get("carAuthorizations"))
            );

            if (Objects.nonNull(getListUserAuthorizationCarRequest.getEmail())) {
                predicates.add(criteriaBuilder.like(
                        root.get("email"), "%" + getListUserAuthorizationCarRequest.getEmail() + "%"
                ));
            }

            if (Objects.nonNull(getListUserAuthorizationCarRequest.getName())) {
                predicates.add(criteriaBuilder.like(
                        root.get("name"), "%" + getListUserAuthorizationCarRequest.getName() + "%"
                ));
            }

            if (Objects.nonNull(getListUserAuthorizationCarRequest.getIsActive())) {
                predicates.add(criteriaBuilder.equal(
                        root.get("isActive"), getListUserAuthorizationCarRequest.getIsActive()
                ));
            }


            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        });

        Pageable pageable = PageRequest.of(getListUserAuthorizationCarRequest.getPage(), getListUserAuthorizationCarRequest.getSize(), Sort.by(Sort.Order.desc("createdAt")));
        Page<User> userWithAuthorizations = userRepository.findAll(specification, pageable);
        List<UserAuthorizationCarResponse> listUserWithCarAuthorization = userWithAuthorizations.stream()
                .map(userData -> {
                    List<CarResponse> listCarResponses = userData.getCarAuthorizations()
                            .stream()
                            .map(carAuthorization -> CarResponse
                                    .builder()
                                    .id(carAuthorization.getCar().getId().toString())
                                    .name(carAuthorization.getCar().getName())
                                    .year(carAuthorization.getCar().getYear())
                                    .price(carAuthorization.getCar().getPricePerDay())
                                    .transmission(carAuthorization.getCar().getTransmission().name())
                                    .imageUrl(carAuthorization.getCar().getImageUrl())
                                    .build())
                            .toList();

                    return UserAuthorizationCarResponse
                            .builder()
                            .id(userData.getId().toString())
                            .name(userData.getName())
                            .phone(userData.getPhoneNumber())
                            .imageUrl(userData.getImageUrl())
                            .email(userData.getEmail())
                            .isActive(userData.getIsActive())
                            .carsAuthorizations(listCarResponses)
                            .build();
                })
                .toList();

        return new PageImpl<>(listUserWithCarAuthorization, pageable ,userWithAuthorizations.getTotalElements());
    }

    @Override
    public UserEditResponse editUser(User user, String userId, UserEditRequest userEditRequest, MultipartFile file) {

        if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
            if (!user.getId().toString().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
            }

            if (Objects.nonNull(userEditRequest.getRole())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access edit role");
            }

            if (Objects.nonNull(userEditRequest.getIsActive())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access edit status active");
            }
        }
        UUID id = uuidUtils.uuidFromString(userId, "user not found");

        User userLoadDB = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        if (Objects.nonNull(userEditRequest.getName())) {
            userLoadDB.setName(userEditRequest.getName().toLowerCase());
        }
        if (Objects.nonNull(userEditRequest.getPhone())) {
            userLoadDB.setPhoneNumber(userEditRequest.getPhone());
        }
        if (Objects.nonNull(userEditRequest.getDob())) {
            try {
                userLoadDB.setDateOfBirth(LocalDateTime.parse(userEditRequest.getDob()).toInstant(ZoneOffset.ofHours(0)));
            }catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid format date");
            }
        }
        if (Objects.nonNull(userEditRequest.getIsActive())) {
            userLoadDB.setIsActive(userEditRequest.getIsActive());
        }
        if (Objects.nonNull(userEditRequest.getRole())){
            userLoadDB.setRole(enumUtils.getUserRoleEnumFromString(userEditRequest.getRole()));
        }
        if (Objects.nonNull(file) && !file.isEmpty()) {
            String pathImage = fileStorageService.storeFile(file, userPath);

            try {
                if (userLoadDB.getImageUrl() != null) {
                    Files.deleteIfExists(Path.of(userLoadDB.getImageUrl()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            userLoadDB.setImageUrl(pathImage);
        }
        User userEditSaved = userRepository.save(userLoadDB);

        return UserEditResponse.builder()
                .id(userEditSaved.getId().toString())
                .name(userEditSaved.getName())
                .image(userEditSaved.getImageUrl())
                .phone(userEditSaved.getPhoneNumber())
                .dob(userEditSaved.getDateOfBirth() != null ? dateTimeUtils.localDateFromInstantZoneJakarta(userEditSaved.getDateOfBirth()).toString() : null)
                .role(userEditSaved.getRole().name())
                .isActive(userEditSaved.getIsActive())
                .build();
    }
}

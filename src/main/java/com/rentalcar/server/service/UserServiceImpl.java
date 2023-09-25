package com.rentalcar.server.service;

import com.rentalcar.server.entity.Transaction;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.*;
import com.rentalcar.server.repository.TransactionRepository;
import com.rentalcar.server.repository.UserRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Transactional
    @Override
    public CreateUserResponse createUser(CreateUserRequest request, MultipartFile file) {

        Optional<User> userByEmail = userRepository.findByEmail(request.getEmail().trim());
        if (userByEmail.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already register");
        }
        Optional<User> userByPhoneNumber = userRepository.findByPhoneNumber(request.getPhone().trim());
        if (userByPhoneNumber.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "phone number already use");
        }

        String pathImage = null;
        if (!file.isEmpty()) {
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
        } catch (Exception e) {
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

    @Override
    public String deleteUserById(User user, String userId) {
        if (user.getRole().equals(UserRoleEnum.USER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
        }

        UUID idUser;
        try {
            idUser = UUID.fromString(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }

        userRepository.findById(idUser).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        userRepository.deleteById(idUser);

        return "success delete data user";

    }

    public Page<GetListUserResponse> getListUser(User user, GetListUserRequest getListUserRequest) {

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
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("role"), getListUserRequest.getRole().equalsIgnoreCase(UserRoleEnum.ADMIN.name()) ? UserRoleEnum.ADMIN : UserRoleEnum.USER
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

        List<GetListUserResponse> responses = pagingUsers.stream()
                .map(userData -> GetListUserResponse
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
    public Page<GetListUserTransactionResponse> getListUserTransaction(User user, GetListUserTransactionRequest getListUserTransactionRequest) {

        getListUserTransactionRequest.setPage(getListUserTransactionRequest.getPage() > 0 ? getListUserTransactionRequest.getPage() - 1 : getListUserTransactionRequest.getPage());

        Pageable pageable = PageRequest.of(getListUserTransactionRequest.getPage(), getListUserTransactionRequest.getSize(), Sort.by(Sort.Order.desc("createdAt")));
        Page<Transaction> transactionsByUserId = transactionRepository.findAllByUserId(user.getId(), pageable);

        List<GetListUserTransactionResponse> responses = transactionsByUserId.stream()
                .map(transaction -> GetListUserTransactionResponse
                        .builder()
                        .id(transaction.getId().toString())
                        .startDate(LocalDateTime.ofInstant(transaction.getStartDate(), ZoneId.of("Asia/Jakarta")).toString())
                        .endDate(LocalDateTime.ofInstant(transaction.getEndDate(), ZoneId.of("Asia/Jakarta")).toString())
                        .duration(transaction.getDuration())
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
}

package com.rentalcar.server.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.rentalcar.server.model.*;
import com.rentalcar.server.util.EnumUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.rentalcar.server.entity.Car;
import com.rentalcar.server.entity.CarRented;
import com.rentalcar.server.entity.Transaction;
import com.rentalcar.server.entity.TransactionStatusEnum;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.repository.CarRentedRepository;
import com.rentalcar.server.repository.CarRepository;
import com.rentalcar.server.repository.TransactionRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.util.DateTimeUtils;
import com.rentalcar.server.util.UUIDUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Value("${image.transaction.payment}")
    private String transactionImagePath;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final CarRentedRepository carRentedRepository;
    private final UUIDUtils uuidUtils;
    private final DateTimeUtils dateTimeUtils;
    private final EnumUtils enumUtils;
    private final FileStorageService fileStorageService;

    @Transactional
    @Override
    public TransactionCreateResponse createTransaction(User user,
                                                       TransactionCreateRequest transactionCreateRequest) {

        var userId = uuidUtils.uuidFromString(transactionCreateRequest.getUserId(), "user not found");
        var carId = uuidUtils.uuidFromString(transactionCreateRequest.getCarId(), "car not found");

        if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
            if (!Objects.equals(user.getId().toString(), userId.toString())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "cannot create transaction to other user");
            }
        }

        var userData = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        var carData = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "car not found"));

        // Format date and time -> yyyy-MM-ddTHH:mm:ss
        var startDate = dateTimeUtils.localDateTimeFromString(transactionCreateRequest.getDateAndTime());
        var endDate = startDate.plusDays(1);
        var isCarAvailable = carIsAvailableToRent(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(startDate),
                dateTimeUtils.instantFromLocalDateTimeZoneJakarta(endDate), carData.getId());

        if (!isCarAvailable) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "car is not available, please select another date");
        }

        // Random number
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(100000);
        String formattedRandomNumber = String.format("%05d", num);

        var noInvoice = "INV/" + startDate.getYear() + "-" + startDate.getMonth() + "-"
                + startDate.getDayOfMonth()
                + "/" + transactionCreateRequest.getDuration() + "/" + formattedRandomNumber;

        // Save transaction
        var carTransactionSave = transactionRepository.save(Transaction.builder()
                .noInvoice(noInvoice)
                .car(carData)
                .user(userData)
                .startDate(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(startDate))
                .endDate(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(endDate))
                .durationDay(transactionCreateRequest.getDuration())
                .carBrand(carData.getBrand())
                .carCapacity(carData.getCapacity())
                .carCc(carData.getCc())
                .carDiscount(carData.getDiscount())
                .carImageUrl(carData.getImageUrl())
                .carName(carData.getName())
                .carPrice(carData.getPricePerDay())
                .carTax(carData.getTax())
                .carYear(carData.getYear())
                .totalPrice(carData.getPricePerDay() * transactionCreateRequest.getDuration())
                .build());

        // Save on table car rented
        carRentedRepository.save(CarRented.builder()
                .car(carData)
                .startDate(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(startDate))
                .endDate(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(endDate))
                .transaction(carTransactionSave)
                .build());

        return TransactionCreateResponse.builder()
                .id(carTransactionSave.getId().toString())
                .noInvoice(carTransactionSave.getNoInvoice())
                .startDate(
                        dateTimeUtils.localDateTimeFromInstantZoneJakarta(
                                carTransactionSave.getStartDate()).toString())
                .endDate(dateTimeUtils
                        .localDateTimeFromInstantZoneJakarta(carTransactionSave.getEndDate())
                        .toString())
                .duration(carTransactionSave.getDurationDay())
                .carName(carTransactionSave.getCarName())
                .carImageUrl(carTransactionSave.getCarImageUrl())
                .brand(carTransactionSave.getCarBrand().name())
                .carYear(carTransactionSave.getCarYear())
                .capacity(carTransactionSave.getCarCapacity())
                .cc(carTransactionSave.getCarCc())
                .price(carTransactionSave.getCarPrice())
                .tax(carTransactionSave.getCarTax())
                .discount(carTransactionSave.getCarDiscount())
                .totalPrice(carTransactionSave.getTotalPrice())
                .build();
    }

    private boolean carIsAvailableToRent(Instant startDate, Instant endDate, UUID carId) {

        List<Car> carList = carRepository.findCarsInDateRange(startDate, endDate);
        boolean isCarFoundRented = carList.stream().anyMatch(carData -> carData.getId() == carId);

        return !isCarFoundRented;
    }

    @Transactional
    @Override
    public String deleteTransactionById(User user, String transactionId) {

        var trId = uuidUtils.uuidFromString(transactionId, "transaction not found");
        if (user.getRole().equals(UserRoleEnum.USER)) {
            var transaction = transactionRepository.findById(trId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "transaction not found"));
            if (transaction.getStatus() != TransactionStatusEnum.WAITING_PAYMENT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "cannot delete transaction because payment has already been made");
            }

            transactionRepository.deleteById(trId);
        }

        if (user.getRole().equals(UserRoleEnum.ADMIN)) {
            transactionRepository.deleteById(trId);
        }

        return "success delete transaction";
    }

    @Override
    public TransactionDetailResponse getDetailTransaction(User user, String transactionId) {

        var trId = uuidUtils.uuidFromString(transactionId, "transaction not found");

        var transaction = transactionRepository.findById(trId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "transaction not found"));
        if (user.getRole().equals(UserRoleEnum.USER)) {
            var userTrId = transaction.getUser().getId();
            if (userTrId != user.getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have a access");
            }

        }

        return TransactionDetailResponse.builder()
                .id(transaction.getId().toString())
                .noInvoice(transaction.getNoInvoice())
                .startDate(dateTimeUtils.localDateFromInstantZoneJakarta(transaction.getStartDate())
                        .toString())
                .endDate(dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getEndDate())
                        .toString())
                .duration(transaction.getDurationDay())
                .carName(transaction.getCarName())
                .carImageUrl(transaction.getCarImageUrl())
                .brand(transaction.getCarBrand().name())
                .year(transaction.getCarYear())
                .capacity(transaction.getCarCapacity())
                .cc(transaction.getCarCc())
                .price(transaction.getCarPrice())
                .tax(transaction.getCarTax())
                .discount(transaction.getCarDiscount())
                .price(transaction.getCarPrice())
                .totalPrice(transaction.getTotalPrice())
                .status(transaction.getStatus().name())
                .userId(transaction.getUser().getId().toString())
                .user(TransactionDetailUserDataResponse.builder()
                        .id(transaction.getUser().getId().toString())
                        .name(transaction.getUser().getName())
                        .email(transaction.getUser().getEmail())
                        .imageUrl(transaction.getUser().getImageUrl())
                        .dob(dateTimeUtils
                                .localDateFromInstantZoneJakarta(
                                        transaction.getUser().getDateOfBirth())
                                .toString())
                        .phone(transaction.getUser().getPhoneNumber())
                        .isActive(transaction.getUser().getIsActive())
                        .role(transaction.getUser().getRole().name())
                        .dateCreated(dateTimeUtils.localDateTimeFromInstantZoneJakarta(
                                transaction.getUser().getCreatedAt()).toString())
                        .build())
                .build();
    }

    @Override
    public Page<TransactionResponse> getListTransaction(User user, TransactionsRequest transactionsRequest) {
        transactionsRequest.setPage(transactionsRequest.getPage() > 0 ? transactionsRequest.getPage() - 1 : transactionsRequest.getPage());


        if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "don't have access");
        }


        if (transactionsRequest.getStartDate() != null || transactionsRequest.getEndDate() != null) {
            if (transactionsRequest.getEndDate() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "end date required for filter by date");
            }

            if (transactionsRequest.getStartDate() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start date required for filter by date");
            }

        }

        Specification<Transaction> specification = ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(transactionsRequest.getStartDate()) && Objects.nonNull(transactionsRequest.getEndDate())) {
                LocalDateTime startDateLocalDateTime = dateTimeUtils.localDateTimeFromString(transactionsRequest.getStartDate());
                LocalDateTime endDateLocalDateTime = dateTimeUtils.localDateTimeFromString(transactionsRequest.getEndDate());

                Instant startDate = dateTimeUtils.instantFromLocalDateTimeZoneJakarta(startDateLocalDateTime);
                Instant endDate = dateTimeUtils.instantFromLocalDateTimeZoneJakarta(endDateLocalDateTime);

                predicates.add(
                        criteriaBuilder.between(root.get("createdAt"), startDate, endDate)
                );
            }

            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        });

        Sort.Order sort;
        if (transactionsRequest.getSort() != null) {
            if (transactionsRequest.getSort().equalsIgnoreCase("asc")) {
                sort = Sort.Order.asc("createdAt");
            } else {
                sort = Sort.Order.desc("createdAt");
            }
        } else {
            sort = Sort.Order.desc("createdAt");
        }

        Pageable pageable = PageRequest.of(transactionsRequest.getPage(), transactionsRequest.getSize(), Sort.by(sort));
        Page<Transaction> transactionsData = transactionRepository.findAll(specification, pageable);

        List<TransactionResponse> transactions = transactionsData.stream()
                .map(transaction -> TransactionResponse.builder()
                        .id(transaction.getId().toString())
                        .noInvoice(transaction.getNoInvoice())
                        .startDate(dateTimeUtils.localDateFromInstantZoneJakarta(transaction.getStartDate()).toString())
                        .endDate(dateTimeUtils.localDateFromInstantZoneJakarta(transaction.getEndDate()).toString())
                        .duration(transaction.getDurationDay())
                        .carName(transaction.getCarName())
                        .brand(transaction.getCarBrand().toString())
                        .totalPrice(transaction.getTotalPrice())
                        .status(transaction.getStatus().toString())
                        .build()
                ).toList();

        return new PageImpl<>(transactions, pageable, transactionsData.getTotalElements());
    }

    @Transactional
    @Override
    public TransactionEditResponse editTransaction(User user, String trxId, Integer status, MultipartFile paymentImage) {

        /*
         * Status On transaction
         * null -> waiting payment
         * 0 -> waiting approve
         * 1 -> approved
         * 2 -> rejected
         * 3 -> on going
         * 4 -> finish
         * */

        // check user
        userRepository.findById(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        // convert id string to UUID
        UUID transactionId = uuidUtils.uuidFromString(trxId, "transaction not found");

        // get transaction by id
        Transaction transactionData = transactionRepository.findById(transactionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "transaction not found"));

        // check status
        if (Objects.isNull(status) || status > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status not found");
        }

        String path = null;
        if (Objects.nonNull(paymentImage) && !paymentImage.isEmpty()) {
            path = fileStorageService.storeFile(paymentImage, transactionImagePath);
            transactionData.setPaymentImage(path);
        }

        transactionData.setStatus(enumUtils.getTransactionEnumFromInteger(status));

        Transaction savedTransaction = transactionRepository.save(transactionData);

        return TransactionEditResponse.builder()
                .paymentImage(path)
                .status(enumUtils.getStatusFromTransactionEnum(savedTransaction.getStatus()))
                .build();
    }

}

package com.rentalcar.server.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.rentalcar.server.entity.Car;
import com.rentalcar.server.entity.CarRented;
import com.rentalcar.server.entity.Transaction;
import com.rentalcar.server.entity.TransactionStatusEnum;
import com.rentalcar.server.entity.User;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.TransactionCreateRequest;
import com.rentalcar.server.model.TransactionCreateResponse;
import com.rentalcar.server.model.TransactionDetailResponse;
import com.rentalcar.server.model.TransactionDetailUserDataResponse;
import com.rentalcar.server.repository.CarRentedRepository;
import com.rentalcar.server.repository.CarRepository;
import com.rentalcar.server.repository.TransactionRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.util.DateTimeUtils;
import com.rentalcar.server.util.EnumUtils;
import com.rentalcar.server.util.UUIDUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

        private final TransactionRepository transactionRepository;
        private final UserRepository userRepository;
        private final CarRepository carRepository;
        private final CarRentedRepository carRentedRepository;
        private final UUIDUtils uuidUtils;
        private final DateTimeUtils dateTimeUtils;
        private final EnumUtils enumUtils;

        @Transactional
        @Override
        public TransactionCreateResponse createTransaction(User user,
                        TransactionCreateRequest transactionCreateRequest) {

                var userId = uuidUtils.uuidFromString(transactionCreateRequest.getUserId(), "User not found");
                var carId = uuidUtils.uuidFromString(transactionCreateRequest.getCarId(), "car not found");
                if (user.getId() != userId) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "cannot create transaction to other user");
                }

                var userData = userRepository.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
                var carData = carRepository.findById(carId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "car not found"));

                var startDate = dateTimeUtils.localDateTimeFromString(transactionCreateRequest.getDateAndTime());
                var endDate = startDate.plusDays(1);
                var isCarAvailble = carIsAvailableToRent(dateTimeUtils.instantFromLocalDateTimeZoneJakarta(startDate),
                                dateTimeUtils.instantFromLocalDateTimeZoneJakarta(endDate), carData.getId());

                if (!isCarAvailble) {
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

}

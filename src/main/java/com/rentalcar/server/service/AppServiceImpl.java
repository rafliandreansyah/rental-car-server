package com.rentalcar.server.service;

import com.rentalcar.server.entity.Transaction;
import com.rentalcar.server.entity.TransactionStatusEnum;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.CarRentedToday;
import com.rentalcar.server.model.DashboardResponse;
import com.rentalcar.server.model.TransactionLastWeek;
import com.rentalcar.server.model.TransactionResponse;
import com.rentalcar.server.repository.CarRepository;
import com.rentalcar.server.repository.TransactionRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.util.DateTimeUtils;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class AppServiceImpl implements AppService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final DateTimeUtils dateTimeUtils;

    @Override
    public DashboardResponse getDashboard() {


        List<Transaction> transactions = transactionRepository.findAll();

        //Total transactions
        double totalTransactions = transactions.stream()
                .filter(transaction -> transaction.getStatus().equals(TransactionStatusEnum.FINISH))
                .mapToDouble(Transaction::getTotalPrice)
                .sum();

        //Total transaction this month
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime endDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        double totalTransactionThisMonth = transactions.stream().
                filter(transaction ->
                        dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getCreatedAt()).isAfter(firstDayOfMonth.minusDays(1))
                                && dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getCreatedAt()).isBefore(endDayOfMonth.plusDays(1))
                )
                .filter(transaction -> transaction.getStatus().equals(TransactionStatusEnum.FINISH))
                .mapToDouble(Transaction::getTotalPrice)
                .sum();

        //Total car rented
        Integer totalCarRented = transactions.size();


        //Total users Role User
        Integer totalUser = (int) userRepository.countByRole(UserRoleEnum.USER).longValue();


        //Transaction By Day in week
        //Today
        DayOfWeek dayOfWeekToday = today.getDayOfWeek();
        long countToday = transactions.stream()
                .filter(transaction -> dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toLocalDate().equals(today.toLocalDate()))
                .count();

        //Today -1 day
        LocalDate todayMinus1Day = today.minusDays(1).toLocalDate();
        DayOfWeek dayOfWeekTodayMinus1Day = todayMinus1Day.getDayOfWeek();
        long countMinus1Day = transactions.stream()
                .filter(transaction -> dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toLocalDate().equals(todayMinus1Day))
                .count();

        //Today -2 day
        LocalDate todayMinus2Day = today.minusDays(2).toLocalDate();
        DayOfWeek dayOfWeekTodayMinus2Day = todayMinus2Day.getDayOfWeek();
        long countMinus2Day = transactions.stream()
                .filter(transaction -> dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toLocalDate().equals(todayMinus1Day))
                .count();

        //Today -3 day
        LocalDate todayMinus3Day = today.minusDays(3).toLocalDate();
        DayOfWeek dayOfWeekTodayMinus3Day = todayMinus3Day.getDayOfWeek();
        long countMinus3Day = transactions.stream()
                .filter(transaction -> dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toLocalDate().equals(todayMinus3Day))
                .count();

        //Today -4 day
        LocalDate todayMinus4Day = today.minusDays(4).toLocalDate();
        DayOfWeek dayOfWeekTodayMinus4Day = todayMinus4Day.getDayOfWeek();
        long countMinus4Day = transactions.stream()
                .filter(transaction -> dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toLocalDate().equals(todayMinus4Day))
                .count();

        //Today -5 day
        LocalDate todayMinus5Day = today.minusDays(5).toLocalDate();
        DayOfWeek dayOfWeekTodayMinus5Day = todayMinus5Day.getDayOfWeek();
        long countMinus5Day = transactions.stream()
                .filter(transaction -> dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toLocalDate().equals(todayMinus5Day))
                .count();

        //Today -6 day
        LocalDate todayMinus6Day = today.minusDays(6).toLocalDate();
        DayOfWeek dayOfWeekTodayMinus6Day = todayMinus6Day.getDayOfWeek();
        long countMinus6Day = transactions.stream()
                .filter(transaction -> dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toLocalDate().equals(todayMinus6Day))
                .count();


        List<Integer> daysOfWeek = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            DayOfWeek dayOfWeek = getDayOfWeek(i);
            daysOfWeek.add(getTransactionCountByDayOffWeek(dayOfWeek,
                    dayOfWeekToday, countToday,
                    dayOfWeekTodayMinus1Day, countMinus1Day,
                    dayOfWeekTodayMinus2Day, countMinus2Day,
                    dayOfWeekTodayMinus3Day, countMinus3Day,
                    dayOfWeekTodayMinus4Day, countMinus4Day,
                    dayOfWeekTodayMinus5Day, countMinus5Day,
                    dayOfWeekTodayMinus6Day, countMinus6Day
            ));
        }
        TransactionLastWeek transactionLastWeek = TransactionLastWeek.builder()
                .sunday(daysOfWeek.get(0))
                .monday(daysOfWeek.get(1))
                .tuesday(daysOfWeek.get(2))
                .wednesday(daysOfWeek.get(3))
                .thursday(daysOfWeek.get(4))
                .friday(daysOfWeek.get(5))
                .saturday(daysOfWeek.get(6))
                .build();


        // Car Rented Today
        Integer countRentedToday = (int)transactions.stream().filter(transaction -> dateTimeUtils.localDateFromInstantZoneJakarta(transaction.getStartDate()).equals(LocalDate.now()))
                .count();
        Integer countCars = (int) carRepository.count();
        CarRentedToday carRentedToday = CarRentedToday.builder()
                .totalCars(countCars)
                .carRentedToday(countRentedToday)
                .build();


        //Transaction Waiting Approve Admin
        List<Transaction> filterTransactionWaitingApprove = transactions.stream()
                .filter(transaction -> transaction.getStatus().equals(TransactionStatusEnum.WAITING_APPROVE))
                .limit(10)
                .toList();
        List<TransactionResponse> transactionWaitingApprove = filterTransactionWaitingApprove.stream().map(transaction -> TransactionResponse.builder()
                .id(transaction.getId().toString())
                .userName(transaction.getUser().getName())
                .brand(transaction.getCarBrand().name())
                .carName(transaction.getCarName())
                .status(transaction.getStatus().name())
                .duration(transaction.getDurationDay())
                .startDate(dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toString())
                .endDate(dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getEndDate()).toString())
                .totalPrice(transaction.getTotalPrice())
                .noInvoice(transaction.getNoInvoice())
                .build()).toList();

        //Transaction Latest
        List<Transaction> filterTransactionLatest = transactions.stream()
                .filter(transaction -> transaction.getStatus() == null)
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .limit(30)
                .toList();
        List<TransactionResponse> transactionLatest = filterTransactionLatest.stream().map(transaction -> TransactionResponse.builder()
                .id(transaction.getId().toString())
                .userName(transaction.getUser().getName())
                .brand(transaction.getCarBrand().name())
                .carName(transaction.getCarName())
                .status(transaction.getStatus().name())
                .duration(transaction.getDurationDay())
                .startDate(dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getStartDate()).toString())
                .endDate(dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getEndDate()).toString())
                .totalPrice(transaction.getTotalPrice())
                .noInvoice(transaction.getNoInvoice())
                .build()).toList();


        return DashboardResponse.builder()
                .totalTransactions(totalTransactions)
                .totalTransactionsThisMonth(totalTransactionThisMonth)
                .totalCarRented(totalCarRented)
                .totalUsers(totalUser)
                .carRentedToday(carRentedToday)
                .transactionLastWeek(transactionLastWeek)
                .transactionWaitingApprove(transactionWaitingApprove)
                .transactionLatest(transactionLatest)
                .build();
    }

    @NotNull
    private static DayOfWeek getDayOfWeek(int i) {
        DayOfWeek dayOfWeek;
        switch (i) {
            case 1 -> {
                dayOfWeek = DayOfWeek.MONDAY;
            }
            case 2 -> {
                dayOfWeek = DayOfWeek.TUESDAY;
            }
            case 3 -> {
                dayOfWeek = DayOfWeek.WEDNESDAY;
            }
            case 4 -> {
                dayOfWeek = DayOfWeek.THURSDAY;
            }
            case 5 -> {
                dayOfWeek = DayOfWeek.FRIDAY;
            }
            case 6 -> {
                dayOfWeek = DayOfWeek.SATURDAY;
            }
            default -> {
                dayOfWeek = DayOfWeek.SUNDAY;
            }
        }
        return dayOfWeek;
    }


    @NotNull
    public Integer getTransactionCountByDayOffWeek(DayOfWeek dayOfWeekReturn,
                                                   DayOfWeek input1, long count1,
                                                   DayOfWeek input2, long count2,
                                                   DayOfWeek input3, long count3,
                                                   DayOfWeek input4, long count4,
                                                   DayOfWeek input5, long count5,
                                                   DayOfWeek input6, long count6,
                                                   DayOfWeek input7, long count7) {
        if (input1.equals(dayOfWeekReturn)) {
            return (int) count1;
        } else if (input2.equals(dayOfWeekReturn)) {
            return (int) count2;
        } else if (input3.equals(dayOfWeekReturn)) {
            return (int) count3;
        } else if (input4.equals(dayOfWeekReturn)) {
            return (int) count4;
        } else if (input5.equals(dayOfWeekReturn)) {
            return (int) count5;
        } else if (input6.equals(dayOfWeekReturn)) {
            return (int) count6;
        } else if (input7.equals(dayOfWeekReturn)) {
            return (int) count7;
        } else {
            return 0;
        }
    }
}

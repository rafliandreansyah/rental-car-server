package com.rentalcar.server.service;

import com.rentalcar.server.entity.Transaction;
import com.rentalcar.server.entity.TransactionStatusEnum;
import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.model.DashboardResponse;
import com.rentalcar.server.repository.TransactionRepository;
import com.rentalcar.server.repository.UserRepository;
import com.rentalcar.server.util.DateTimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@AllArgsConstructor
public class AppServiceImpl implements AppService{

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final DateTimeUtils dateTimeUtils;

    @Override
    public DashboardResponse getDashboard() {


        List<Transaction> transactions = transactionRepository.findAll();

        //Total transactions
        double totalTransactions = transactions.stream().mapToDouble(Transaction::getTotalPrice).sum();

        //Total transaction this month
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime endDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        double totalTransactionThisMonth = transactions.stream().
                filter(transaction ->
                        dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getCreatedAt()).isAfter(firstDayOfMonth.minusDays(1))
                                && dateTimeUtils.localDateTimeFromInstantZoneJakarta(transaction.getCreatedAt()).isBefore(endDayOfMonth.plusDays(1))
                )
                .mapToDouble(Transaction::getTotalPrice)
                .sum();

        //Total car rented
        Integer totalCarRented = transactions.size();

        //Total users Role User
        Long totalUser = userRepository.countByRole(UserRoleEnum.USER);

        //Transaction By Day in week


        //Transaction Waiting Approve Admin
        List<Transaction> transactionWaitingApprove = transactions.stream().filter(transaction -> transaction.getStatus().equals(TransactionStatusEnum.WAITING_APPROVE)).toList();

        //Transaction Latest
        List<Transaction> transactionLatest = transactions.stream().filter(transaction -> transaction.getStatus() == null).toList();

        return null;
    }
}

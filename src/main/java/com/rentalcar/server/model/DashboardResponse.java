package com.rentalcar.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    @JsonProperty("total_transactions")
    private Double totalTransactions;

    @JsonProperty("total_transactions_this_month")
    private Double totalTransactionsThisMonth;

    @JsonProperty("total_car_rented")
    private Integer totalCarRented;

    @JsonProperty("total_users")
    private Integer totalUsers;

    @JsonProperty("car_rented_today")
    private CarRentedToday carRentedToday;

    @JsonProperty("transaction_last_week")
    private TransactionLastWeek transactionLastWeek;

    @JsonProperty("transaction_waiting_approve")
    private List<TransactionResponse> transactionWaitingApprove;

    @JsonProperty("transaction_latest")
    private List<TransactionResponse> transactionLatest;

}

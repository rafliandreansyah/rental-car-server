package com.rentalcar.server.entity;

public enum TransactionStatusEnum {
    WAITING_PAYMENT,
    WAITING_APPROVE,
    APPROVED,
    REJECTED,
    ON_GOING,
    FINISH
}

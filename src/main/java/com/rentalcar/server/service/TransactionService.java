package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.TransactionCreateRequest;
import com.rentalcar.server.model.TransactionCreateResponse;

/**
 * TransactionService
 */
public interface TransactionService {

    public TransactionCreateResponse createTransaction(User user, TransactionCreateRequest transactionCreateRequest);

    public String deleteTransactionById(User user, String transactionId);

}

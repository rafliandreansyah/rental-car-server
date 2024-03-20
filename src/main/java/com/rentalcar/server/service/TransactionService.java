package com.rentalcar.server.service;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

/**
 * TransactionService
 */
public interface TransactionService {

    public TransactionCreateResponse createTransaction(User user, TransactionCreateRequest transactionCreateRequest);

    public String deleteTransactionById(User user, String transactionId);

    public TransactionDetailResponse getDetailTransaction(User user, String transactionId);

    public Page<TransactionResponse> getListTransaction(User user, TransactionsRequest transactionsRequest);

    public TransactionEditResponse editTransaction(User user, String trxId, String status, MultipartFile paymentImage);

}

package com.rentalcar.server.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.TransactionCreateRequest;
import com.rentalcar.server.model.TransactionCreateResponse;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<WebResponse<TransactionCreateResponse>> createTransaction(User user,
            @RequestBody TransactionCreateRequest transactionCreateRequest) {
        var transactionResponse = transactionService.createTransaction(user, transactionCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebResponse.<TransactionCreateResponse>builder()
                .status(HttpStatus.CREATED.value())
                .data(transactionResponse)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WebResponse<String>> deleteTransaction(User user, @PathVariable("id") String id) {
        var deleteTransactionResponse = transactionService.deleteTransactionById(user, id);
        return ResponseEntity.ok().body(WebResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .data(deleteTransactionResponse)
                .build());
    }

}

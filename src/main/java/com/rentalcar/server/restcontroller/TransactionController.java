package com.rentalcar.server.restcontroller;

import com.rentalcar.server.model.*;
import com.rentalcar.server.model.base.WebResponsePaging;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentalcar.server.entity.User;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.service.TransactionService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<TransactionCreateResponse>> createTransaction(User user,
                                                                                    @RequestBody TransactionCreateRequest transactionCreateRequest) {
        var transactionResponse = transactionService.createTransaction(user, transactionCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebResponse.<TransactionCreateResponse>builder()
                .status(HttpStatus.CREATED.value())
                .data(transactionResponse)
                .build());
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<String>> deleteTransaction(User user, @PathVariable("id") String id) {
        var deleteTransactionResponse = transactionService.deleteTransactionById(user, id);
        return ResponseEntity.ok().body(WebResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .data(deleteTransactionResponse)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WebResponse<TransactionDetailResponse>> getDetailTransaction(User user,
                                                                                       @PathVariable("id") String id) {
        var detailTransactionResponse = transactionService.getDetailTransaction(user, id);
        return ResponseEntity.ok().body(WebResponse.<TransactionDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .data(detailTransactionResponse)
                .build());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponsePaging<List<TransactionResponse>>> getListTransaction(
            User user,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "start_date", required = false) String startDate,
            @RequestParam(name = "end_date", required = false) String endDate,
            @RequestParam(name = "order_by_date_created", required = false) String order
    ) {

        TransactionsRequest transactionsRequest = TransactionsRequest.builder()
                .page(page)
                .size(size)
                .startDate(startDate)
                .endDate(endDate)
                .sort(order)
                .build();
        Page<TransactionResponse> listTransaction = transactionService.getListTransaction(user, transactionsRequest);
        return ResponseEntity.ok().body(WebResponsePaging.<List<TransactionResponse>>builder()
                .totalItem(listTransaction.getTotalElements())
                .perPage(listTransaction.getSize())
                .currentPage(listTransaction.getNumber() + 1)
                .lastPage(listTransaction.getTotalPages())
                .status(HttpStatus.OK.value())
                .data(listTransaction.getContent())
                .build()
        );
    }

    @PatchMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<WebResponse<TransactionEditResponse>> editTransaction(
            User user,
            @PathVariable("id") String trxId,
            @RequestParam("status") Integer status,
            @RequestParam(value = "payment_image", required = false) MultipartFile paymentImage
    ) {
        TransactionEditResponse transactionEditResponse = transactionService.editTransaction(user, trxId, status, paymentImage);

        return ResponseEntity.ok().body(WebResponse.<TransactionEditResponse>builder()
                .status(HttpStatus.OK.value())
                .data(transactionEditResponse)
                .build()
        );

    }

}

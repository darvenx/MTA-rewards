package com.training.controller;


import com.training.dto.transaction.TransactionsDto;
import com.training.dto.transaction.TransferRequestDto;
import com.training.service.impl.TransactionServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TransactionController {

    private final TransactionServiceImpl transactionService;

    public TransactionController(TransactionServiceImpl transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<List<TransactionsDto>> getAllTransactions(@PathVariable Long id){
        return new ResponseEntity<>(transactionService.getTransactions(id), HttpStatus.OK);
    }

    @PostMapping("/transaction")
    public ResponseEntity<Boolean> transferMoney(@RequestBody TransferRequestDto transferRequestDto){
        return new ResponseEntity<>(transactionService.transferMoney(transferRequestDto),HttpStatus.OK);
    }
}

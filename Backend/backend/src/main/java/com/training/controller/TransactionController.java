package com.training.controller;


import com.training.dto.transaction.RecentTransactionsDto;
import com.training.dto.transaction.TransactionsDto;
import com.training.dto.transaction.TransferRequestDto;
import com.training.service.impl.TransactionServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
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

    @GetMapping("/transactions")
    public ResponseEntity<List<RecentTransactionsDto>> getLast10Transactions(){
        return new ResponseEntity<>(transactionService.getRecent10Transactions(),HttpStatus.OK);
    }
}

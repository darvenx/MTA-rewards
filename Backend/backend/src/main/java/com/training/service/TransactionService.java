package com.training.service;

import com.training.exceptions.AccountNotFoundException;
import com.training.exceptions.IncorrectPinException;
import com.training.exceptions.InsufficientBalanceException;
import com.training.dto.transaction.TransactionsDto;
import com.training.dto.transaction.TransferRequestDto;

import java.util.List;

public interface TransactionService {
    Boolean transferMoney(TransferRequestDto transferRequestDto)
            throws AccountNotFoundException, InsufficientBalanceException,
            IncorrectPinException;

    List<TransactionsDto> getTransactions(Long accountNumber)
            throws AccountNotFoundException;
}
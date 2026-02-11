package com.training.controller;

import com.training.dto.transaction.TransactionsDto;
import com.training.dto.transaction.TransferRequestDto;
import com.training.exceptions.AccountNotFoundException;
import com.training.exceptions.IncorrectPinException;
import com.training.exceptions.InsufficientBalanceException;
import com.training.service.TransactionService;
import com.training.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TransactionController Tests")
class TransactionControllerTest {

    @Mock
    private TransactionServiceImpl transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private TransferRequestDto transferRequestDto;
    private List<TransactionsDto> transactionsList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transferRequestDto = new TransferRequestDto();
        transferRequestDto.setSenderAccountNumber(1L);
        transferRequestDto.setReceiverAccountNumber(2L);
        transferRequestDto.setSenderAccountPin("1234");
        transferRequestDto.setAmount(500.0);
        transferRequestDto.setIdempotencyKey("key-123");

        TransactionsDto txn1 = new TransactionsDto();
        txn1.setOtherAccountName("Bob");

        TransactionsDto txn2 = new TransactionsDto();
        txn2.setOtherAccountName("Alice");

        transactionsList = Arrays.asList(txn1, txn2);
    }

    @Test
    @DisplayName("Should successfully retrieve all transactions for an account")
    void testGetAllTransactions_Success() throws AccountNotFoundException {
        when(transactionService.getTransactions(1L))
                .thenReturn(transactionsList);

        ResponseEntity<List<TransactionsDto>> response = transactionController.getAllTransactions(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Bob", response.getBody().get(0).getOtherAccountName());

        verify(transactionService, times(1)).getTransactions(1L);
    }

    @Test
    @DisplayName("Should retrieve empty transaction list when no transactions exist")
    void testGetAllTransactions_Empty() throws AccountNotFoundException {
        when(transactionService.getTransactions(1L))
                .thenReturn(new ArrayList<>());

        ResponseEntity<List<TransactionsDto>> response = transactionController.getAllTransactions(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());

        verify(transactionService, times(1)).getTransactions(1L);
    }

    @Test
    @DisplayName("Should retrieve transactions for different account")
    void testGetAllTransactions_DifferentAccount() throws AccountNotFoundException {
        TransactionsDto txn = new TransactionsDto();
        txn.setOtherAccountName("David");
        List<TransactionsDto> transactions = Arrays.asList(txn);

        when(transactionService.getTransactions(10L))
                .thenReturn(transactions);

        ResponseEntity<List<TransactionsDto>> response = transactionController.getAllTransactions(10L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("David", response.getBody().get(0).getOtherAccountName());

        verify(transactionService, times(1)).getTransactions(10L);
    }

    @Test
    @DisplayName("Should successfully transfer money between accounts")
    void testTransferMoney_Success() throws Exception {
        when(transactionService.transferMoney(any(TransferRequestDto.class)))
                .thenReturn(true);

        ResponseEntity<Boolean> response = transactionController.transferMoney(transferRequestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());

        verify(transactionService, times(1)).transferMoney(any(TransferRequestDto.class));
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when sender account doesn't exist")
    void testTransferMoney_SenderAccountNotFound() {
        when(transactionService.transferMoney(any(TransferRequestDto.class)))
                .thenThrow(new AccountNotFoundException());

        assertThrows(AccountNotFoundException.class, () -> transactionController.transferMoney(transferRequestDto));

        verify(transactionService, times(1)).transferMoney(any(TransferRequestDto.class));
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when account has insufficient balance")
    void testTransferMoney_InsufficientBalance() {
        when(transactionService.transferMoney(any(TransferRequestDto.class)))
                .thenThrow(new InsufficientBalanceException());

        assertThrows(InsufficientBalanceException.class, () -> transactionController.transferMoney(transferRequestDto));

        verify(transactionService, times(1)).transferMoney(any(TransferRequestDto.class));
    }

    @Test
    @DisplayName("Should throw IncorrectPinException when PIN is incorrect")
    void testTransferMoney_IncorrectPin() {
        when(transactionService.transferMoney(any(TransferRequestDto.class)))
                .thenThrow(new IncorrectPinException());

        assertThrows(IncorrectPinException.class, () -> transactionController.transferMoney(transferRequestDto));

        verify(transactionService, times(1)).transferMoney(any(TransferRequestDto.class));
    }

    @Test
    @DisplayName("Should transfer money with different amounts")
    void testTransferMoney_DifferentAmount() throws Exception {
        transferRequestDto.setAmount(1000.0);
        when(transactionService.transferMoney(any(TransferRequestDto.class)))
                .thenReturn(true);

        ResponseEntity<Boolean> response = transactionController.transferMoney(transferRequestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());

        verify(transactionService, times(1)).transferMoney(any(TransferRequestDto.class));
    }

    @Test
    @DisplayName("Should return false when transfer fails")
    void testTransferMoney_Failure() throws Exception {
        when(transactionService.transferMoney(any(TransferRequestDto.class)))
                .thenReturn(false);

        ResponseEntity<Boolean> response = transactionController.transferMoney(transferRequestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());

        verify(transactionService, times(1)).transferMoney(any(TransferRequestDto.class));
    }
}

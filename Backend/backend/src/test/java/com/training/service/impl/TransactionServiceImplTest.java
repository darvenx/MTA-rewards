package com.training.service.impl;

import com.training.dto.transaction.TransactionsDto;
import com.training.dto.transaction.TransferRequestDto;
import com.training.entities.Account;
import com.training.entities.Transaction;
import com.training.entities.User;
import com.training.enums.AccountStatus;
import com.training.enums.AccountType;
import com.training.enums.TransactionStatus;
import com.training.exceptions.AccountNotFoundException;
import com.training.exceptions.IncorrectPinException;
import com.training.exceptions.InsufficientBalanceException;
import com.training.repo.AccountRepo;
import com.training.repo.TransactionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TransactionServiceImpl Tests")
class TransactionServiceImplTest {

    @Mock
    private TransactionRepo transactionRepo;

    @Mock
    private AccountRepo accountRepo;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account senderAccount;
    private Account receiverAccount;
    private TransferRequestDto transferRequestDto;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user1 = new User();
        user1.setUserId(1L);

        senderAccount = new Account();
        senderAccount.setAccountId(1L);
        senderAccount.setAccountBalance(5000.0);
        senderAccount.setAccountType(AccountType.SAVINGS);
        senderAccount.setAccountStatus(AccountStatus.ACTIVE);
        senderAccount.setUser(user1);

        receiverAccount = new Account();
        receiverAccount.setAccountId(2L);
        receiverAccount.setAccountBalance(3000.0);
        receiverAccount.setAccountType(AccountType.CURRENT);
        receiverAccount.setAccountStatus(AccountStatus.ACTIVE);
        receiverAccount.setUser(user1);

        transferRequestDto = new TransferRequestDto();
        transferRequestDto.setSenderAccountNumber(1L);
        transferRequestDto.setReceiverAccountNumber(2L);
        transferRequestDto.setAmount(500.0);
        transferRequestDto.setSenderAccountPin("1234");
        transferRequestDto.setIdempotencyKey("key-123");

        transaction = new Transaction();
        transaction.setTransactionId(1L);
        transaction.setFromAccount(1L);
        transaction.setToAccount(2L);
        transaction.setAmount(500.0);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should successfully transfer money between accounts")
    void testTransferMoney_Success() throws AccountNotFoundException, IncorrectPinException, InsufficientBalanceException {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiverAccount));

        Boolean result = transactionService.transferMoney(transferRequestDto);

        assertTrue(result);
        assertEquals(4500.0, senderAccount.getAccountBalance());
        assertEquals(3500.0, receiverAccount.getAccountBalance());

        verify(accountRepo, times(1)).findById(1L);
        verify(accountRepo, times(1)).findById(2L);
        verify(accountRepo, times(1)).save(senderAccount);
        verify(accountRepo, times(1)).save(receiverAccount);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when sender account doesn't exist")
    void testTransferMoney_SenderNotFound() {
        when(accountRepo.findById(1L)).thenReturn(Optional.empty());
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiverAccount));

        assertThrows(AccountNotFoundException.class, () -> transactionService.transferMoney(transferRequestDto));

        verify(accountRepo, times(1)).findById(1L);
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when receiver account doesn't exist")
    void testTransferMoney_ReceiverNotFound() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transactionService.transferMoney(transferRequestDto));

        verify(accountRepo, times(1)).findById(1L);
        verify(accountRepo, times(1)).findById(2L);
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when sender has low balance")
    void testTransferMoney_InsufficientBalance() {
        senderAccount.setAccountBalance(100.0);
        when(accountRepo.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiverAccount));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.transferMoney(transferRequestDto));

        verify(accountRepo, times(1)).findById(1L);
        verify(accountRepo, times(1)).findById(2L);
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should successfully transfer exact balance amount")
    void testTransferMoney_ExactBalance()
            throws AccountNotFoundException, IncorrectPinException, InsufficientBalanceException {
        transferRequestDto.setAmount(5000.0);
        when(accountRepo.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiverAccount));

        Boolean result = transactionService.transferMoney(transferRequestDto);

        assertTrue(result);
        assertEquals(0.0, senderAccount.getAccountBalance());
        assertEquals(8000.0, receiverAccount.getAccountBalance());

        verify(accountRepo, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should handle transfer with small amount")
    void testTransferMoney_SmallAmount()
            throws AccountNotFoundException, IncorrectPinException, InsufficientBalanceException {
        transferRequestDto.setAmount(0.01);
        when(accountRepo.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiverAccount));

        Boolean result = transactionService.transferMoney(transferRequestDto);

        assertTrue(result);
        assertEquals(4999.99, senderAccount.getAccountBalance());
        assertEquals(3000.01, receiverAccount.getAccountBalance());
    }

    @Test
    @DisplayName("Should handle transfer with large amount")
    void testTransferMoney_LargeAmount()
            throws AccountNotFoundException, IncorrectPinException, InsufficientBalanceException {
        senderAccount.setAccountBalance(1000000.0);
        transferRequestDto.setAmount(500000.0);
        when(accountRepo.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiverAccount));

        Boolean result = transactionService.transferMoney(transferRequestDto);

        assertTrue(result);
        assertEquals(500000.0, senderAccount.getAccountBalance());
        assertEquals(503000.0, receiverAccount.getAccountBalance());
    }

    @Test
    @DisplayName("Should successfully retrieve all transactions for an account")
    void testGetTransactions_Success() throws AccountNotFoundException {
        List<Transaction> transactions = Arrays.asList(transaction);

        // Setup mock return for account lookup
        Account otherAccount = new Account();
        otherAccount.setAccountId(2L);
        otherAccount.setAccountHolderName("Receiver");

        when(transactionRepo.findAllByFromAccountOrToAccount(1L, 1L))
                .thenReturn(transactions);

        Account selfAccount = new Account();
        selfAccount.setAccountId(1L);
        selfAccount.setAccountHolderName("Self");

        when(accountRepo.findById(1L)).thenReturn(Optional.of(selfAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(otherAccount));

        List<TransactionsDto> result = transactionService.getTransactions(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(transactionRepo, times(1)).findAllByFromAccountOrToAccount(1L, 1L);
    }

    @Test
    @DisplayName("Should return empty list when no transactions exist")
    void testGetTransactions_Empty() throws AccountNotFoundException {
        when(transactionRepo.findAllByFromAccountOrToAccount(1L, 1L))
                .thenReturn(new ArrayList<>());

        List<TransactionsDto> result = transactionService.getTransactions(1L);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(transactionRepo, times(1)).findAllByFromAccountOrToAccount(1L, 1L);
    }

    @Test
    @DisplayName("Should correctly map other account numbers in transactions")
    void testGetTransactions_MappingOtherAccountNumbers() throws AccountNotFoundException {
        Transaction txn1 = new Transaction();
        txn1.setFromAccount(1L);
        txn1.setToAccount(2L);
        txn1.setAmount(100.0);
        txn1.setTransactionStatus(TransactionStatus.SUCCESS);

        Transaction txn2 = new Transaction();
        txn2.setFromAccount(3L);
        txn2.setToAccount(1L);
        txn2.setAmount(200.0);
        txn2.setTransactionStatus(TransactionStatus.SUCCESS);

        Account account2 = new Account();
        account2.setAccountId(2L);
        account2.setAccountHolderName("Bob");

        Account account3 = new Account();
        account3.setAccountId(3L);
        account3.setAccountHolderName("Charlie");

        when(transactionRepo.findAllByFromAccountOrToAccount(1L, 1L))
                .thenReturn(Arrays.asList(txn1, txn2));

        Account selfAccount = new Account();
        selfAccount.setAccountId(1L);
        selfAccount.setAccountHolderName("Self");

        when(accountRepo.findById(1L)).thenReturn(Optional.of(selfAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(account2));
        when(accountRepo.findById(3L)).thenReturn(Optional.of(account3));

        List<TransactionsDto> result = transactionService.getTransactions(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Bob", result.get(0).getOtherAccountName());
        assertEquals("Charlie", result.get(1).getOtherAccountName());
    }

    @Test
    @DisplayName("Should retrieve transactions for different account")
    void testGetTransactions_DifferentAccount() throws AccountNotFoundException {
        Transaction txn = new Transaction();
        txn.setFromAccount(10L);
        txn.setToAccount(11L);
        txn.setAmount(100.0);
        txn.setTransactionStatus(TransactionStatus.SUCCESS);

        Account account11 = new Account();
        account11.setAccountId(11L);
        account11.setAccountHolderName("David");

        when(transactionRepo.findAllByFromAccountOrToAccount(10L, 10L))
                .thenReturn(Arrays.asList(txn));

        Account account10 = new Account();
        account10.setAccountId(10L);
        account10.setAccountHolderName("Me");

        when(accountRepo.findById(10L)).thenReturn(Optional.of(account10));
        when(accountRepo.findById(11L)).thenReturn(Optional.of(account11));

        List<TransactionsDto> result = transactionService.getTransactions(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("David", result.get(0).getOtherAccountName());

        verify(transactionRepo, times(1)).findAllByFromAccountOrToAccount(10L, 10L);
    }

    @Test
    @DisplayName("Should handle multiple transactions for single account")
    void testGetTransactions_MultipleTransactions() throws AccountNotFoundException {
        Transaction txn1 = new Transaction();
        txn1.setFromAccount(1L);
        txn1.setToAccount(2L);
        txn1.setAmount(100.0);
        txn1.setTransactionStatus(TransactionStatus.SUCCESS);

        Transaction txn2 = new Transaction();
        txn2.setFromAccount(1L);
        txn2.setToAccount(3L);
        txn2.setAmount(100.0);
        txn2.setTransactionStatus(TransactionStatus.SUCCESS);

        Transaction txn3 = new Transaction();
        txn3.setFromAccount(4L);
        txn3.setToAccount(1L);
        txn3.setAmount(100.0);
        txn3.setTransactionStatus(TransactionStatus.SUCCESS);

        Account account2 = new Account();
        account2.setAccountId(2L);
        account2.setAccountHolderName("Two");

        Account account3 = new Account();
        account3.setAccountId(3L);
        account3.setAccountHolderName("Three");

        Account account4 = new Account();
        account4.setAccountId(4L);
        account4.setAccountHolderName("Four");

        when(transactionRepo.findAllByFromAccountOrToAccount(1L, 1L))
                .thenReturn(Arrays.asList(txn1, txn2, txn3));

        Account selfAccount = new Account();
        selfAccount.setAccountId(1L);
        selfAccount.setAccountHolderName("Self");

        when(accountRepo.findById(1L)).thenReturn(Optional.of(selfAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(account2));
        when(accountRepo.findById(3L)).thenReturn(Optional.of(account3));
        when(accountRepo.findById(4L)).thenReturn(Optional.of(account4));

        List<TransactionsDto> result = transactionService.getTransactions(1L);

        assertNotNull(result);
        assertEquals(3, result.size());

        verify(transactionRepo, times(1)).findAllByFromAccountOrToAccount(1L, 1L);
    }

    @Test
    @DisplayName("Should save both accounts after successful transfer")
    void testTransferMoney_BothAccountsSaved() throws AccountNotFoundException, IncorrectPinException, InsufficientBalanceException {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiverAccount));

        transactionService.transferMoney(transferRequestDto);

        verify(accountRepo, times(1)).save(senderAccount);
        verify(accountRepo, times(1)).save(receiverAccount);
    }

    @Test
    @DisplayName("Should handle receiver account with zero balance after transfer")
    void testTransferMoney_ReceiverZeroBalance()
            throws AccountNotFoundException, IncorrectPinException, InsufficientBalanceException {
        receiverAccount.setAccountBalance(0.0);
        transferRequestDto.setAmount(500.0);

        when(accountRepo.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiverAccount));

        Boolean result = transactionService.transferMoney(transferRequestDto);

        assertTrue(result);
        assertEquals(0.0, receiverAccount.getAccountBalance() - 500.0);
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when balance equals zero")
    void testTransferMoney_ZeroBalance() {
        senderAccount.setAccountBalance(0.0);
        when(accountRepo.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiverAccount));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.transferMoney(transferRequestDto));
    }
}

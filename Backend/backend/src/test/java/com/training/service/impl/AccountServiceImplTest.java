package com.training.service.impl;

import com.training.dto.account.AccountCreateDto;
import com.training.dto.account.AccountSuccessCreation;
import com.training.entities.Account;
import com.training.entities.User;
import com.training.enums.AccountStatus;
import com.training.enums.AccountType;
import com.training.repo.AccountRepo;
import com.training.repo.UserRepo;
import com.training.dto.AccountDataDto;
import com.training.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("AccountServiceImpl Tests")
class AccountServiceImplTest {

    @Mock
    private AccountRepo accountRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User testUser;
    private Account testAccount;
    private AccountCreateDto accountCreateDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setPhoneNumber("1234567890");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setAccountHolderName("Test User");
        testAccount.setAccountBalance(10000.0);
        testAccount.setAccountType(AccountType.SAVINGS);
        testAccount.setAccountStatus(AccountStatus.ACTIVE);
        testAccount.setUser(testUser);
        testAccount.setLastUpdated(LocalDateTime.now());

        accountCreateDto = new AccountCreateDto();
        accountCreateDto.setUserId(1L);
        accountCreateDto.setAccountHolderName("Test User");
        accountCreateDto.setAccountType(AccountType.SAVINGS);
    }

    @Test
    @DisplayName("Should successfully create a new account with default balance 10000")
    void testCreateAccount_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepo.saveAndFlush(any(Account.class))).thenReturn(testAccount);
        when(accountRepo.findAllByUser_UserId(1L)).thenReturn(Arrays.asList(testAccount));

        AccountSuccessCreation result = accountService.createAccount(accountCreateDto);

        assertNotNull(result);
        assertEquals(1, result.getAccountNumbers().size());
        assertEquals(1L, result.getAccountNumbers().get(0));
        assertEquals(10000.0, result.getAccountBalance().get(0));
        assertEquals(AccountType.SAVINGS, result.getAccountType().get(0));
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus().get(0));

        verify(userRepo, times(1)).findById(1L);
        verify(accountRepo, times(1)).saveAndFlush(any(Account.class));
        verify(accountRepo, times(1)).findAllByUser_UserId(1L);
    }

    @Test
    @DisplayName("Should create account with ACTIVE status by default")
    void testCreateAccount_DefaultStatus() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepo.saveAndFlush(any(Account.class))).thenReturn(testAccount);
        when(accountRepo.findAllByUser_UserId(1L)).thenReturn(Arrays.asList(testAccount));

        AccountSuccessCreation result = accountService.createAccount(accountCreateDto);

        assertNotNull(result);
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus().get(0));

        verify(accountRepo, times(1)).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("Should create multiple accounts for same user")
    void testCreateAccount_MultipleAccounts() {
        Account account2 = new Account();
        account2.setAccountId(2L);
        account2.setAccountHolderName("Test User");
        account2.setAccountBalance(10000.0);
        account2.setAccountType(AccountType.CURRENT);
        account2.setAccountStatus(AccountStatus.ACTIVE);
        account2.setUser(testUser);

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepo.saveAndFlush(any(Account.class))).thenReturn(testAccount);
        when(accountRepo.findAllByUser_UserId(1L)).thenReturn(Arrays.asList(testAccount, account2));

        AccountSuccessCreation result = accountService.createAccount(accountCreateDto);

        assertNotNull(result);
        assertEquals(2, result.getAccountNumbers().size());
        assertEquals(2, result.getAccountBalance().size());
        assertEquals(2, result.getAccountType().size());

        verify(accountRepo, times(1)).findAllByUser_UserId(1L);
    }

    @Test
    @DisplayName("Should return account numbers correctly")
    void testCreateAccount_AccountNumbers() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepo.saveAndFlush(any(Account.class))).thenReturn(testAccount);
        when(accountRepo.findAllByUser_UserId(1L)).thenReturn(Arrays.asList(testAccount));

        AccountSuccessCreation result = accountService.createAccount(accountCreateDto);

        assertNotNull(result);
        assertTrue(result.getAccountNumbers().contains(1L));

        verify(accountRepo, times(1)).findAllByUser_UserId(1L);
    }

    @Test
    @DisplayName("Should return correct balances in response")
    void testCreateAccount_Balances() {
        Account account1 = new Account();
        account1.setAccountId(1L);
        account1.setAccountBalance(5000.0);
        account1.setAccountType(AccountType.SAVINGS);
        account1.setAccountStatus(AccountStatus.ACTIVE);

        Account account2 = new Account();
        account2.setAccountId(2L);
        account2.setAccountBalance(3000.0);
        account2.setAccountType(AccountType.CURRENT);
        account2.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepo.saveAndFlush(any(Account.class))).thenReturn(account1);
        when(accountRepo.findAllByUser_UserId(1L)).thenReturn(Arrays.asList(account1, account2));

        AccountSuccessCreation result = accountService.createAccount(accountCreateDto);

        assertNotNull(result);
        assertEquals(2, result.getAccountBalance().size());
        assertEquals(5000.0, result.getAccountBalance().get(0));
        assertEquals(3000.0, result.getAccountBalance().get(1));
    }

    @Test
    @DisplayName("Should return correct account types")
    void testCreateAccount_AccountTypes() {
        Account savingsAccount = new Account();
        savingsAccount.setAccountId(1L);
        savingsAccount.setAccountType(AccountType.SAVINGS);
        savingsAccount.setAccountStatus(AccountStatus.ACTIVE);

        Account currentAccount = new Account();
        currentAccount.setAccountId(2L);
        currentAccount.setAccountType(AccountType.CURRENT);
        currentAccount.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepo.saveAndFlush(any(Account.class))).thenReturn(savingsAccount);
        when(accountRepo.findAllByUser_UserId(1L)).thenReturn(Arrays.asList(savingsAccount, currentAccount));

        AccountSuccessCreation result = accountService.createAccount(accountCreateDto);

        assertNotNull(result);
        assertTrue(result.getAccountType().contains(AccountType.SAVINGS));
        assertTrue(result.getAccountType().contains(AccountType.CURRENT));
    }

    @Test
    @DisplayName("Should verify account is active when it exists")
    void testIsActive_AccountExists() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(testAccount));

        Boolean result = accountService.isActive(1L);

        assertTrue(result);

        verify(accountRepo, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return false when account doesn't exist")
    void testIsActive_AccountNotFound() {
        when(accountRepo.findById(999L)).thenReturn(Optional.empty());

        Boolean result = accountService.isActive(999L);

        assertFalse(result);

        verify(accountRepo, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should return true for existing account ID")
    void testIsActive_ExistingId() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(testAccount));

        Boolean result = accountService.isActive(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false for non-existing account ID")
    void testIsActive_NonExistingId() {
        when(accountRepo.findById(100L)).thenReturn(Optional.empty());

        Boolean result = accountService.isActive(100L);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should check multiple accounts status")
    void testIsActive_MultipleChecks() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.empty());

        Boolean result1 = accountService.isActive(1L);
        Boolean result2 = accountService.isActive(2L);

        assertTrue(result1);
        assertFalse(result2);

        verify(accountRepo, times(1)).findById(1L);
        verify(accountRepo, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should create account with correct timestamp")
    void testCreateAccount_WithTimestamp() {
        LocalDateTime beforeCall = LocalDateTime.now();

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepo.saveAndFlush(any(Account.class))).thenReturn(testAccount);
        when(accountRepo.findAllByUser_UserId(1L)).thenReturn(Arrays.asList(testAccount));

        AccountSuccessCreation result = accountService.createAccount(accountCreateDto);

        LocalDateTime afterCall = LocalDateTime.now();

        assertNotNull(result);
        assertNotNull(testAccount.getLastUpdated());
        assertTrue(testAccount.getLastUpdated().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(testAccount.getLastUpdated().isBefore(afterCall.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should return account details successfully")
    void testGetAccountDetails_Success() throws Exception {
        when(accountRepo.findAllByUser_UserId(1L)).thenReturn(Arrays.asList(testAccount));

        AccountDataDto result = accountService.getAccountDetails(1L);

        assertNotNull(result);
        assertTrue(result.getAccountIds().contains(1L));
        assertTrue(result.getBalances().contains(10000.0));

        verify(accountRepo, times(1)).findAllByUser_UserId(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when no accounts found")
    void testGetAccountDetails_UserNotFound() {
        when(accountRepo.findAllByUser_UserId(1L)).thenReturn(new ArrayList<>());

        assertThrows(UserNotFoundException.class, () -> accountService.getAccountDetails(1L));

        verify(accountRepo, times(1)).findAllByUser_UserId(1L);
    }
}

package com.training.controller;

import com.training.dto.account.AccountCreateDto;
import com.training.dto.account.AccountDataDto;
import com.training.dto.account.AccountSuccessCreation;
import com.training.enums.AccountStatus;
import com.training.enums.AccountType;
import com.training.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AccountController Tests")
class AccountControllerTest {

    @Mock
    private AccountServiceImpl accountService;

    @InjectMocks
    private AccountController accountController;

    private AccountCreateDto accountCreateDto;
    private AccountSuccessCreation accountSuccessCreation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        accountCreateDto = new AccountCreateDto();

        accountSuccessCreation = new AccountSuccessCreation();
        accountSuccessCreation.setAccountNumbers(Arrays.asList(1L, 2L));
        accountSuccessCreation.setAccountBalance(Arrays.asList(10000.0, 10000.0));
        accountSuccessCreation.setAccountType(Arrays.asList(AccountType.SAVINGS, AccountType.CURRENT));
        accountSuccessCreation.setAccountStatus(Arrays.asList(AccountStatus.ACTIVE, AccountStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should successfully create a new account")
    void testCreateNewAccount_Success() {
        when(accountService.createAccount(any(AccountCreateDto.class)))
                .thenReturn(accountSuccessCreation);

        ResponseEntity<AccountSuccessCreation> response = accountController.createNewAccount(accountCreateDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getAccountNumbers().size());
        assertEquals(1L, response.getBody().getAccountNumbers().get(0));
        assertEquals(10000.0, response.getBody().getAccountBalance().get(0));

        verify(accountService, times(1)).createAccount(any(AccountCreateDto.class));
    }

    @Test
    @DisplayName("Should create account with single account in list")
    void testCreateNewAccount_SingleAccount() {
        AccountSuccessCreation singleAccountResponse = new AccountSuccessCreation();
        singleAccountResponse.setAccountNumbers(Arrays.asList(1L));
        singleAccountResponse.setAccountBalance(Arrays.asList(10000.0));
        singleAccountResponse.setAccountType(Arrays.asList(AccountType.SAVINGS));
        singleAccountResponse.setAccountStatus(Arrays.asList(AccountStatus.ACTIVE));

        when(accountService.createAccount(any(AccountCreateDto.class)))
                .thenReturn(singleAccountResponse);

        ResponseEntity<AccountSuccessCreation> response = accountController.createNewAccount(accountCreateDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getAccountNumbers().size());
        assertEquals(1L, response.getBody().getAccountNumbers().get(0));

        verify(accountService, times(1)).createAccount(any(AccountCreateDto.class));
    }

    @Test
    @DisplayName("Should create account with default balance of 10000")
    void testCreateNewAccount_DefaultBalance() {
        AccountSuccessCreation responseDto = new AccountSuccessCreation();
        responseDto.setAccountNumbers(Arrays.asList(100L));
        responseDto.setAccountBalance(Arrays.asList(10000.0));
        responseDto.setAccountType(Arrays.asList(AccountType.SAVINGS));
        responseDto.setAccountStatus(Arrays.asList(AccountStatus.ACTIVE));

        when(accountService.createAccount(any(AccountCreateDto.class)))
                .thenReturn(responseDto);

        ResponseEntity<AccountSuccessCreation> response = accountController.createNewAccount(accountCreateDto);

        assertNotNull(response);
        assertEquals(10000.0, response.getBody().getAccountBalance().get(0));

        verify(accountService, times(1)).createAccount(any(AccountCreateDto.class));
    }

    @Test
    @DisplayName("Should create account with ACTIVE status")
    void testCreateNewAccount_ActiveStatus() {
        AccountSuccessCreation responseDto = new AccountSuccessCreation();
        responseDto.setAccountNumbers(Arrays.asList(1L));
        responseDto.setAccountBalance(Arrays.asList(10000.0));
        responseDto.setAccountType(Arrays.asList(AccountType.SAVINGS));
        responseDto.setAccountStatus(Arrays.asList(AccountStatus.ACTIVE));

        when(accountService.createAccount(any(AccountCreateDto.class)))
                .thenReturn(responseDto);

        ResponseEntity<AccountSuccessCreation> response = accountController.createNewAccount(accountCreateDto);

        assertNotNull(response);
        assertEquals(AccountStatus.ACTIVE, response.getBody().getAccountStatus().get(0));

        verify(accountService, times(1)).createAccount(any(AccountCreateDto.class));
    }

    @Test
    @DisplayName("Should successfully fetch all accounts data")
    void testFetchAllAccountsData_Success() {
        // Arrange
        AccountDataDto accountDataDto = new AccountDataDto();
        accountDataDto.addAccount(1L);
        accountDataDto.addBalance(1000.0);

        when(accountService.getAllAccountsData(1L)).thenReturn(accountDataDto);

        // Act
        ResponseEntity<AccountDataDto> response = accountController.fetchAllAccountsData(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getAccountIds().contains(1L));
        assertTrue(response.getBody().getBalances().contains(1000.0));

        verify(accountService, times(1)).getAllAccountsData(1L);
    }
}

package com.training.controller;

import com.training.dto.user.UserLoginDto;
import com.training.dto.user.UserSignUpDto;
import com.training.dto.user.UserSuccessLoginOrSignUpDto;
import com.training.exceptions.UserAlreadyExistsException;
import com.training.exceptions.UserNotFoundException;
import com.training.service.UserService;
import com.training.service.impl.UserServiceImpl;
import com.training.service.impl.AccountServiceImpl;
import com.training.dto.AccountDataDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserServiceImpl userService;

    @Mock
    private AccountServiceImpl accountService;

    @InjectMocks
    private UserController userController;

    private UserSignUpDto userSignUpDto;
    private UserLoginDto userLoginDto;
    private UserSuccessLoginOrSignUpDto successDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userSignUpDto = UserSignUpDto.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .phoneNumber("1234567890")
                .firstName("Test")
                .lastName("User")
                .build();

        userLoginDto = UserLoginDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        successDto = new UserSuccessLoginOrSignUpDto();
        successDto.setToken("token123");
    }

    @Test
    @DisplayName("Should successfully sign up a new user")
    void testSignUpNewUser_Success() throws UserAlreadyExistsException {
        when(userService.signUp(any(UserSignUpDto.class))).thenReturn(successDto);

        ResponseEntity<UserSuccessLoginOrSignUpDto> response = userController.signUpNewUser(userSignUpDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token123", response.getBody().getToken());

        verify(userService, times(1)).signUp(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when signing up with existing username")
    void testSignUpNewUser_UserAlreadyExists() {
        when(userService.signUp(any(UserSignUpDto.class)))
                .thenThrow(new UserAlreadyExistsException());

        assertThrows(UserAlreadyExistsException.class, () -> userController.signUpNewUser(userSignUpDto));

        verify(userService, times(1)).signUp(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("Should successfully login an existing user")
    void testLoginUser_Success() throws UserNotFoundException {
        UserSuccessLoginOrSignUpDto loginResponseDto = new UserSuccessLoginOrSignUpDto();
        loginResponseDto.setToken("jwt-token");

        when(userService.login(any(UserLoginDto.class))).thenReturn(loginResponseDto);

        ResponseEntity<UserSuccessLoginOrSignUpDto> response = userController.loginUser(userLoginDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().getToken());

        verify(userService, times(1)).login(any(UserLoginDto.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when logging in with invalid credentials")
    void testLoginUser_UserNotFound() {
        when(userService.login(any(UserLoginDto.class)))
                .thenThrow(new UserNotFoundException());

        UserLoginDto invalidLoginDto = UserLoginDto.builder()
                .username("invalid")
                .password("wrong")
                .build();
        assertThrows(UserNotFoundException.class, () -> userController.loginUser(invalidLoginDto));

        verify(userService, times(1)).login(any(UserLoginDto.class));
    }

    @Test
    @DisplayName("Should successfully update user details")
    void testUpdateDetails_Success() throws Exception {
        when(userService.updateData(any(UserSignUpDto.class))).thenReturn(true);

        ResponseEntity<Boolean> response = userController.updateDetails(userSignUpDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());

        verify(userService, times(1)).updateData(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when updating non-existent user")
    void testUpdateDetails_UserNotFound() {
        when(userService.updateData(any(UserSignUpDto.class)))
                .thenThrow(new UserNotFoundException());

        assertThrows(UserNotFoundException.class, () -> userController.updateDetails(userSignUpDto));

        verify(userService, times(1)).updateData(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("Should return false when updating user fails")
    void testUpdateDetails_Failure() throws Exception {
        when(userService.updateData(any(UserSignUpDto.class))).thenReturn(false);

        ResponseEntity<Boolean> response = userController.updateDetails(userSignUpDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());

        verify(userService, times(1)).updateData(any(UserSignUpDto.class));
    }

    @Test
    @DisplayName("Should successfully get account details")
    void testGetAccounts_Success() throws UserNotFoundException {
        AccountDataDto accountDataDto = new AccountDataDto();
        accountDataDto.addAccount(1L);
        accountDataDto.addBalance(1000.0);

        when(accountService.getAccountDetails(1L)).thenReturn(accountDataDto);

        ResponseEntity<AccountDataDto> response = userController.getAccounts(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getAccountIds().contains(1L));
        assertTrue(response.getBody().getBalances().contains(1000.0));

        verify(accountService, times(1)).getAccountDetails(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getting accounts for non-existent user")
    void testGetAccounts_UserNotFound() throws UserNotFoundException {
        when(accountService.getAccountDetails(1L))
                .thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> userController.getAccounts(1L));

        verify(accountService, times(1)).getAccountDetails(1L);
    }

    @Test
    @DisplayName("Should successfully get user details")
    void testGetUserDetails_Success() throws UserNotFoundException {
        // Arrange
        com.training.dto.UserDetailsResponseDto userDetails = new com.training.dto.UserDetailsResponseDto(
                "Test User", "test@example.com", "1234567890", "testuser");

        when(userService.getUserDetails(1L)).thenReturn(userDetails);

        // Act
        ResponseEntity<com.training.dto.UserDetailsResponseDto> response = userController.getUserDetails(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test User", response.getBody().getFullName());
        assertEquals("test@example.com", response.getBody().getEmail());

        verify(userService, times(1)).getUserDetails(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getting details for non-existent user")
    void testGetUserDetails_UserNotFound() throws UserNotFoundException {
        // Arrange
        when(userService.getUserDetails(1L)).thenThrow(new UserNotFoundException());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userController.getUserDetails(1L));

        verify(userService, times(1)).getUserDetails(1L);
    }
}

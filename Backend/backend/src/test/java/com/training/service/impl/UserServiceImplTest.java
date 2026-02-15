package com.training.service.impl;

import com.training.dto.user.*;
import com.training.entities.Account;
import com.training.entities.User;
import com.training.enums.AccountStatus;
import com.training.enums.AccountType;
import com.training.exceptions.UserAlreadyExistsException;
import com.training.exceptions.UserNotFoundException;
import com.training.repo.UserRepo;
import com.training.jwt.JwtService;
import com.training.jwt.Jwt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

        @Mock
        private UserRepo userRepo;

        @Mock
        private JwtService jwtService;

        @InjectMocks
        private UserServiceImpl userService;

        private User testUser;
        private UserSignUpDto signUpDto;
        private UserLoginDto loginDto;
        private UserUpdatePasswordDto updatePasswordDto;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);

                testUser = new User();
                testUser.setUserId(1L);
                testUser.setUsername("testuser");
                testUser.setPassword("oldPassword123");
                testUser.setEmail("test@example.com");
                testUser.setPhoneNumber("1234567890");
                testUser.setFirstName("Test");
                testUser.setLastName("User");

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

                testUser.setAccounts(Arrays.asList(account1, account2));

                signUpDto = UserSignUpDto.builder()
                                .username("newuser")
                                .password("password123")
                                .email("newuser@example.com")
                                .phoneNumber("9876543210")
                                .firstName("New")
                                .lastName("User")
                                .build();

                loginDto = UserLoginDto.builder()
                                .username("testuser")
                                .password("oldPassword123")
                                .build();

                updatePasswordDto = new UserUpdatePasswordDto();
                updatePasswordDto.setUsername("testuser");
                updatePasswordDto.setConfirmPassword("oldPassword123");
                updatePasswordDto.setNewPassword("newPassword456");
        }

    @Test
    @DisplayName("Should successfully login user with correct credentials")
    void testLogin_Success() {
        when(userRepo.findByUsernameAndPassword("testuser", "oldPassword123"))
                .thenReturn(Optional.of(testUser));

        Jwt jwt = mock(Jwt.class);
        when(jwt.toString()).thenReturn("token123");
        when(jwtService.generateAccessToken(anyLong(), anyString(), anyList(), any())).thenReturn(jwt);

        UserSuccessLoginOrSignUpDto result = userService.login(loginDto);

        assertNotNull(result);
        assertEquals("token123", result.getToken());

        verify(userRepo, times(1)).findByUsernameAndPassword("testuser", "oldPassword123");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when credentials are invalid")
    void testLogin_InvalidCredentials() {
        when(userRepo.findByUsernameAndPassword("invaliduser", "wrongpassword"))
                .thenReturn(Optional.empty());

        UserLoginDto invalidLoginDto = UserLoginDto.builder()
                .username("invaliduser")
                .password("wrongpassword")
                .build();

        assertThrows(UserNotFoundException.class, () -> userService.login(invalidLoginDto));
        verify(userRepo, times(1)).findByUsernameAndPassword("invaliduser", "wrongpassword");
    }

        @Test
        @DisplayName("Should return empty account list for user with no accounts")
        void testLogin_UserWithNoAccounts() {
                User userNoAccounts = new User();
                userNoAccounts.setUserId(2L);
                userNoAccounts.setUsername("testuser");
                userNoAccounts.setPassword("oldPassword123");
                userNoAccounts.setAccounts(new ArrayList<>());

                when(userRepo.findByUsernameAndPassword("testuser", "oldPassword123"))
                                .thenReturn(Optional.of(userNoAccounts));

                Jwt jwt = mock(Jwt.class);
                when(jwt.toString()).thenReturn("token123");
                when(jwtService.generateAccessToken(anyLong(), anyString(), anyList(), any()))
                                .thenReturn(jwt);

                UserSuccessLoginOrSignUpDto result = userService.login(loginDto);

                assertNotNull(result);
                assertEquals("token123", result.getToken());
        }

    @Test
    @DisplayName("Should successfully sign up new user")
    void testSignUp_Success() throws UserAlreadyExistsException {
        when(userRepo.findByUsernameOrEmailOrPhoneNumber("newuser", "newuser@example.com", "9876543210"))
                .thenReturn(Optional.empty());

        when(userRepo.saveAndFlush(any(User.class))).thenReturn(testUser);

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setUsername("newuser");
        savedUser.setPassword("password123");
        savedUser.setAccounts(new ArrayList<>());

        when(userRepo.findByUsernameAndPassword("newuser", "password123"))
                .thenReturn(Optional.of(savedUser));

        Jwt jwt = mock(Jwt.class);
        when(jwt.toString()).thenReturn("token123");
        when(jwtService.generateAccessToken(anyLong(), anyString(), anyList(), any())).thenReturn(jwt);

        UserSuccessLoginOrSignUpDto result = userService.signUp(signUpDto);

        assertNotNull(result);
        assertEquals("token123", result.getToken());

        verify(userRepo, times(1)).findByUsernameOrEmailOrPhoneNumber("newuser", "newuser@example.com", "9876543210");
        verify(userRepo, times(1)).saveAndFlush(any(User.class));
        verify(userRepo, times(1)).findByUsernameAndPassword("newuser", "password123");
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username already exists")
    void testSignUp_UserAlreadyExists_Username() {
        when(userRepo.findByUsernameOrEmailOrPhoneNumber("newuser", "newuser@example.com", "9876543210"))
                .thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.signUp(signUpDto));

        verify(userRepo, times(1)).findByUsernameOrEmailOrPhoneNumber("newuser", "newuser@example.com", "9876543210");
        verify(userRepo, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("Should successfully update user details")
    void testUpdateData_Success() {
        when(userRepo.findByUsernameAndPassword("testuser", "oldPassword123"))
                .thenReturn(Optional.of(testUser));

        UserSignUpDto updateDto = UserSignUpDto.builder()
                .username("testuser")
                .password("oldPassword123")
                .email("newemail@example.com")
                .phoneNumber("1111111111")
                .firstName("UpdatedFirst")
                .lastName("UpdatedLast")
                .build();

        Boolean result = userService.updateData(updateDto);

        assertTrue(result);
        assertEquals("newemail@example.com", testUser.getEmail());
        assertEquals("1111111111", testUser.getPhoneNumber());
        assertEquals("UpdatedFirst", testUser.getFirstName());
        assertEquals("UpdatedLast", testUser.getLastName());

        verify(userRepo, times(1)).findByUsernameAndPassword("testuser", "oldPassword123");
        verify(userRepo, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when updating non-existent user")
    void testUpdateData_UserNotFound() {
        when(userRepo.findByUsernameAndPassword("invaliduser", "wrongpassword"))
                .thenReturn(Optional.empty());

        UserSignUpDto updateDto = UserSignUpDto.builder()
                .username("invaliduser")
                .password("wrongpassword")
                .email("newemail@example.com")
                .phoneNumber("1111111111")
                .firstName("UpdatedFirst")
                .lastName("UpdatedLast")
                .build();

        assertThrows(UserNotFoundException.class, () -> userService.updateData(updateDto));
        verify(userRepo, times(1)).findByUsernameAndPassword("invaliduser", "wrongpassword");
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update only non-empty fields")
    void testUpdateData_PartialUpdate() {
        when(userRepo.findByUsernameAndPassword("testuser", "oldPassword123"))
                .thenReturn(Optional.of(testUser));

        UserSignUpDto partialUpdateDto = UserSignUpDto.builder()
                .username("testuser")
                .password("oldPassword123")
                .email("newemail@example.com")
                .phoneNumber("")
                .firstName("")
                .lastName("")
                .build();

        userService.updateData(partialUpdateDto);

        assertEquals("newemail@example.com", testUser.getEmail());
        assertEquals("1234567890", testUser.getPhoneNumber()); // Should remain unchanged
        assertEquals("Test", testUser.getFirstName()); // Should remain unchanged

        verify(userRepo, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should successfully reset password")
    void testResetPassword_Success() throws UserNotFoundException {
        when(userRepo.findByUsernameAndPassword("testuser", "oldPassword123"))
                .thenReturn(Optional.of(testUser));

        Boolean result = userService.resetPassword(updatePasswordDto);

        assertTrue(result);
        assertEquals("newPassword456", testUser.getPassword());

        verify(userRepo, times(1)).findByUsernameAndPassword("testuser", "oldPassword123");
        verify(userRepo, times(1)).saveAndFlush(testUser);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when resetting password with wrong old password")
    void testResetPassword_WrongOldPassword() {
        when(userRepo.findByUsernameAndPassword("testuser", "wrongOldPassword"))
                .thenReturn(Optional.empty());

        updatePasswordDto.setConfirmPassword("wrongOldPassword");

        assertThrows(UserNotFoundException.class, () -> userService.resetPassword(updatePasswordDto));
        verify(userRepo, times(1)).findByUsernameAndPassword("testuser", "wrongOldPassword");
        verify(userRepo, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("Should return user details when user exists")
    void testGetUserDetails_Success() throws UserNotFoundException {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        UserDetailsResponseDto result = userService.getUserDetails(1L);

        assertNotNull(result);
        assertEquals("TestUser", result.getFullName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhoneNumber());
        assertEquals("testuser", result.getUserName());

        verify(userRepo, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getting details for non-existent user")
    void testGetUserDetails_UserNotFound() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserDetails(999L));

        verify(userRepo, times(1)).findById(999L);
    }
}

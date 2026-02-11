package com.training.controller;

import com.training.dto.AccountDataDto;
import com.training.dto.user.UserLoginDto;
import com.training.dto.user.UserSignUpDto;
import com.training.dto.user.UserSuccessLoginOrSignUpDto;
import com.training.exceptions.*;
import com.training.service.impl.AccountServiceImpl;
import com.training.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserServiceImpl userService;
    private final AccountServiceImpl accountService;

    public UserController(UserServiceImpl userService,AccountServiceImpl accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserSuccessLoginOrSignUpDto> signUpNewUser(
            @RequestBody UserSignUpDto userSignUpDto)
            throws UserAlreadyExistsException {
        return new ResponseEntity<>(userService.signUp(userSignUpDto), HttpStatus.OK);
    }



    @GetMapping("/user")
    public ResponseEntity<UserSuccessLoginOrSignUpDto> loginUser(
            @RequestBody UserLoginDto userLoginDto)
            throws UserNotFoundException
    {
        return new ResponseEntity<>(userService.login(userLoginDto),HttpStatus.OK);
    }

    @PutMapping("/user")
    public ResponseEntity<Boolean> updateDetails(
            @RequestBody UserSignUpDto userSignUpDto)
            throws DuplicateKeyException,UserNotFoundException {
        return new ResponseEntity<>(userService.updateData(userSignUpDto),HttpStatus.OK);
    }

    @GetMapping("/user/account/{id}")
    public ResponseEntity<AccountDataDto> getAccounts(@PathVariable Long id)
    throws UserNotFoundException
    {
        return new ResponseEntity<>(accountService.getAccountDetails(id),HttpStatus.OK);
    }

}

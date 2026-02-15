package com.training.controller;

import com.training.dto.account.AccountDataDto;
import com.training.dto.user.*;
import com.training.exceptions.*;
import com.training.service.impl.AccountServiceImpl;
import com.training.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserServiceImpl userService;
    private final AccountServiceImpl accountService;

    public UserController(UserServiceImpl userService,AccountServiceImpl accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserSuccessLoginOrSignUpDto> signUpNewUser(
            @RequestBody UserSignUpDto userSignUpDto)
            throws UserAlreadyExistsException {
        return new ResponseEntity<>(userService.signUp(userSignUpDto), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<UserSuccessLoginOrSignUpDto> loginUser(
            @RequestBody UserLoginDto userLoginDto)
            throws UserNotFoundException
    {
        return new ResponseEntity<>(userService.login(userLoginDto),HttpStatus.OK);
    }



    @PutMapping("/update")
    public ResponseEntity<Boolean> updateDetails(
            @RequestBody UserSignUpDto userSignUpDto)
            throws DuplicateKeyException,UserNotFoundException {
        return new ResponseEntity<>(userService.updateData(userSignUpDto),HttpStatus.OK);
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<AccountDataDto> getAccounts(@PathVariable Long id)
            throws UserNotFoundException
    {
        return new ResponseEntity<>(accountService.getAccountDetails(id),HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsResponseDto> getUserDetails(@PathVariable Long id)
            throws UserNotFoundException
    {
        return new ResponseEntity<>(userService.getUserDetails(id),HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<List<AllUserData>> getAllUserData(){
        return new ResponseEntity<>(userService.getAllUserDetails(),HttpStatus.OK);
    }

    @PostMapping("/update-creds/{id}")
    public ResponseEntity<Boolean> updatecredentials(UserUpdatePasswordDto req){
        return new ResponseEntity<>(userService.updateCredentials(req),HttpStatus.OK);
    }

}

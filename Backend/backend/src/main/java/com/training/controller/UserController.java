package com.training.controller;

import com.training.dto.user.UserLoginDto;
import com.training.dto.user.UserSignUpDto;
import com.training.dto.user.UserSuccessLoginOrSignUpDto;
import com.training.exceptions.*;
import com.training.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserSuccessLoginOrSignUpDto> signUpNewUser(
            @RequestBody UserSignUpDto userSignUpDto)
            throws UserAlreadyExistsException {
        return new ResponseEntity<>(userService.singUp(userSignUpDto), HttpStatus.OK);
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

}

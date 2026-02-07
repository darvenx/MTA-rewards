package com.training.controller;

import com.training.dto.account.AccountCreateDto;
import com.training.dto.account.AccountSuccessCreation;
import com.training.service.impl.AccountServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AccountController {

    private final AccountServiceImpl accountService;

    public AccountController(AccountServiceImpl accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/account")
    public ResponseEntity<AccountSuccessCreation> createNewAccount(
            @RequestBody AccountCreateDto accountCreateDto){
        return  new ResponseEntity<>(accountService.createAccount(accountCreateDto),HttpStatus.OK);
    }

}

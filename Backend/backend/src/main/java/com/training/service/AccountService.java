package com.training.service;

import com.training.dto.account.AccountCreateDto;
import com.training.dto.account.AccountSuccessCreation;


public interface AccountService{
    AccountSuccessCreation createAccount(AccountCreateDto accountCreateDto);

    Boolean isActive(Long accountNumber);

}

package com.training.dto.account;

import com.training.enums.AccountType;


public class AccountCreateDto {
    private String accountHolderName;
    private AccountType accountType;
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }
    public AccountType getAccountType() {
        return accountType;
    }
}

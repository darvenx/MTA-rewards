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

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

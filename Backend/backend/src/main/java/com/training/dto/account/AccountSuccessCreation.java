package com.training.dto.account;

import com.training.enums.AccountStatus;
import com.training.enums.AccountType;

import java.util.List;

public class AccountSuccessCreation {
    private List<Long> accountNumbers;
    private List<Double> accountBalance;
    private List<AccountType> accountType;
    private List<AccountStatus> accountStatus;

    public List<Long> getAccountNumbers() {
        return accountNumbers;
    }

    public void setAccountNumbers(List<Long> accountNumbers) {
        this.accountNumbers = accountNumbers;
    }

    public List<Double> getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(List<Double> accountBalance) {
        this.accountBalance = accountBalance;
    }

    public List<AccountType> getAccountType() {
        return accountType;
    }

    public void setAccountType(List<AccountType> accountType) {
        this.accountType = accountType;
    }

    public List<AccountStatus> getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(List<AccountStatus> accountStatus) {
        this.accountStatus = accountStatus;
    }
}

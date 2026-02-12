package com.training.dto;

import java.util.ArrayList;
import java.util.List;

public class AccountDataDto {
    List<Double> balances;
    List<Long> accountIds;
    List<String> accountTypes;
    List<String> accountStatus;

    public List<String> getAccountType() {
        return accountTypes;
    }

    public void setAccountType(List<String> accountType) {
        this.accountTypes = accountType;
    }

    public List<String> getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(List<String> accountStatus) {
        this.accountStatus = accountStatus;
    }

    public List<Double> getBalances() {
        return balances;
    }

    public void setBalances(List<Double> balances) {
        this.balances = balances;
    }

    public List<Long> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<Long> accountIds) {
        this.accountIds = accountIds;
    }

    public AccountDataDto(List<Double> balances, List<Long> accountIds) {
        this.balances = balances;
        this.accountIds = accountIds;
    }

    public AccountDataDto() {
        this.accountIds = new ArrayList<>();
        this.balances = new ArrayList<>();
        this.accountTypes = new ArrayList<>();
        this.accountStatus = new ArrayList<>();
    }

    public void addBalance(Double amount) {
        balances.add(amount);
    }

    public void addAccount(Long id) {
        accountIds.add(id);
    }

    public void addStatus(String status) {
        accountStatus.add(status);
    }

    public void addType(String type) {
        accountTypes.add(type);
    }

    public List<String> getTypes() {
        return accountTypes;
    }

    public List<String> getStatuses() {
        return accountStatus;
    }
}

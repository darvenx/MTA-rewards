package com.training.dto;

import java.util.ArrayList;
import java.util.List;

public class AccountDataDto {
    List<Double> balances;
    List<Long> accountIds;

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
    }
    public void addBalance(Double amount){
        balances.add(amount);
    }
    public void addAccount(Long id){
        accountIds.add(id);
    }
}

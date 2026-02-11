package com.training.dto.user;

import java.util.List;

public class UserSuccessLoginOrSignUpDto {
    private String token;
    private List<Double> balances;
    private List<Long> accounts;
    private Long id;

    public List<Double> getBalances() {
        return balances;
    }

    public void setBalances(List<Double> balances) {
        this.balances = balances;
    }

    public List<Long> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Long> accounts) {
        this.accounts = accounts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
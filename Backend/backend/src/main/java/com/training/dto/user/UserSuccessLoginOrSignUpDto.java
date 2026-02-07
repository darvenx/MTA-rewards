package com.training.dto.user;


import java.util.List;

public class UserSuccessLoginOrSignUpDto {
    private Long userId;
    private String token;
    private List<Double> accountBalance;
    private List<Long> accountNumbers;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Double> getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(List<Double> accountBalance) {
        this.accountBalance = accountBalance;
    }

    public List<Long> getAccountNumbers() {
        return accountNumbers;
    }

    public void setAccountNumbers(List<Long> accountNumbers) {
        this.accountNumbers = accountNumbers;
    }

}

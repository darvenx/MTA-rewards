package com.training.dto;

import java.util.List;

public class AllUserData {
    private Long userId;
    private List<Long> accountIds;
    private List<Integer> accountTransactionCount;
    private String accountRole;

    public AllUserData(Long userId, List<Long> accountIds, List<Integer> accountTransactionCount, String accountStatus) {
        this.userId = userId;
        this.accountIds = accountIds;
        this.accountTransactionCount = accountTransactionCount;
        this.accountRole = accountStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<Long> accountIds) {
        this.accountIds = accountIds;
    }

    public List<Integer> getAccountTransactionCount() {
        return accountTransactionCount;
    }

    public void setAccountTransactionCount(List<Integer> accountTransactionCount) {
        this.accountTransactionCount = accountTransactionCount;
    }

    public String getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(String accountRole) {
        this.accountRole = accountRole;
    }
}

package com.training.dto.analytics;

import java.util.Map;

public class SpendingAnalyticsDto {
    private Long accountId;
    private Map<String, Double> spendingByCategory;
    private int transactionCount;

    public SpendingAnalyticsDto() {
    }

    public SpendingAnalyticsDto(Long accountId, Map<String, Double> spendingByCategory, int transactionCount) {
        this.accountId = accountId;
        this.spendingByCategory = spendingByCategory;
        this.transactionCount = transactionCount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Map<String, Double> getSpendingByCategory() {
        return spendingByCategory;
    }

    public void setSpendingByCategory(Map<String, Double> spendingByCategory) {
        this.spendingByCategory = spendingByCategory;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }
}

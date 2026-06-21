package com.training.dto.reward;

public class RewardSummaryDto {

    private Long accountId;
    private int totalPoints;

    public RewardSummaryDto() {
    }

    public RewardSummaryDto(Long accountId, int totalPoints) {
        this.accountId = accountId;
        this.totalPoints = totalPoints;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}

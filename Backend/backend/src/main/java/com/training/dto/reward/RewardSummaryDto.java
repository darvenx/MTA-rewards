package com.training.dto.reward;

public class RewardSummaryDto {

    private Long accountId;
    private int totalPoints;
    private String currentTier;
    private double multiplier;
    private int nextTierProgress; // 0-100 percentage

    public RewardSummaryDto() {
    }

    public RewardSummaryDto(Long accountId, int totalPoints, String currentTier, double multiplier,
            int nextTierProgress) {
        this.accountId = accountId;
        this.totalPoints = totalPoints;
        this.currentTier = currentTier;
        this.multiplier = multiplier;
        this.nextTierProgress = nextTierProgress;
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

    public String getCurrentTier() {
        return currentTier;
    }

    public void setCurrentTier(String currentTier) {
        this.currentTier = currentTier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public int getNextTierProgress() {
        return nextTierProgress;
    }

    public void setNextTierProgress(int nextTierProgress) {
        this.nextTierProgress = nextTierProgress;
    }
}

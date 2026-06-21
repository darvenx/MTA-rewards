package com.training.dto.reward;

import java.time.LocalDateTime;

public class RewardLogDto {

    private Long rewardId;
    private Long transactionId;
    private int pointsEarned;
    private LocalDateTime createdOn;

    public RewardLogDto() {
    }

    public RewardLogDto(Long rewardId, Long transactionId, int pointsEarned, LocalDateTime createdOn) {
        this.rewardId = rewardId;
        this.transactionId = transactionId;
        this.pointsEarned = pointsEarned;
        this.createdOn = createdOn;
    }

    public Long getRewardId() {
        return rewardId;
    }

    public void setRewardId(Long rewardId) {
        this.rewardId = rewardId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
}

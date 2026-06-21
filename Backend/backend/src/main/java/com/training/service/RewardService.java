package com.training.service;

import com.training.dto.reward.RewardLogDto;
import com.training.dto.reward.RewardSummaryDto;

import java.util.List;

public interface RewardService {

    /**
     * Evaluates reward eligibility for a completed transfer and persists a
     * RewardLog entry if the transaction qualifies.
     *
     * Eligibility rules:
     * 1. Status is SUCCESS (caller is responsible for only invoking after success)
     * 2. amount > 100
     * 3. fromAccountId != toAccountId (self-transfer already blocked upstream)
     *
     * Points = floor(amount / 100)
     */
    void calculateAndSaveReward(Long fromAccountId, Long toAccountId, Double amount, Long transactionId);

    /** Returns the reward history for a given account, newest first. */
    List<RewardLogDto> getRewardsByAccount(Long accountId);

    /** Returns the total accumulated reward points for a given account. */
    RewardSummaryDto getTotalPoints(Long accountId);

    /**
     * Redeems all unredeemed reward points for an account and adds them to the
     * account balance. Minimum 100 points required.
     */
    RewardSummaryDto redeemPoints(Long accountId);
}

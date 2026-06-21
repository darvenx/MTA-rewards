package com.training.controller;

import com.training.dto.reward.RewardLogDto;
import com.training.dto.reward.RewardSummaryDto;
import com.training.service.RewardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    /**
     * GET /api/v1/rewards/{accountId}
     * Returns the reward history for a given account (newest first).
     */
    @GetMapping("/rewards/{accountId}")
    public ResponseEntity<List<RewardLogDto>> getRewardHistory(@PathVariable Long accountId) {
        return new ResponseEntity<>(rewardService.getRewardsByAccount(accountId), HttpStatus.OK);
    }

    /**
     * GET /api/v1/rewards/{accountId}/total
     * Returns the total accumulated reward points for a given account.
     */
    @GetMapping("/rewards/{accountId}/total")
    public ResponseEntity<RewardSummaryDto> getTotalPoints(@PathVariable Long accountId) {
        return new ResponseEntity<>(rewardService.getTotalPoints(accountId), HttpStatus.OK);
    }

    /**
     * POST /api/v1/rewards/{accountId}/redeem
     * Redeems unredeemed points for account balance.
     */
    @PostMapping("/rewards/{accountId}/redeem")
    public ResponseEntity<RewardSummaryDto> redeemPoints(@PathVariable Long accountId) {
        return new ResponseEntity<>(rewardService.redeemPoints(accountId), HttpStatus.OK);
    }
}

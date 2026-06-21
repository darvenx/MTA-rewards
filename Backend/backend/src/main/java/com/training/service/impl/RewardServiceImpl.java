package com.training.service.impl;

import com.training.dto.reward.RewardLogDto;
import com.training.dto.reward.RewardSummaryDto;
import com.training.entities.RewardLog;
import com.training.entities.Account;
import com.training.repo.RewardRepo;
import com.training.repo.AccountRepo;
import com.training.service.RewardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RewardServiceImpl implements RewardService {

    private final RewardRepo rewardRepo;
    private final AccountRepo accountRepo;

    public RewardServiceImpl(RewardRepo rewardRepo, AccountRepo accountRepo) {
        this.rewardRepo = rewardRepo;
        this.accountRepo = accountRepo;
    }

    @Override
    public void calculateAndSaveReward(Long fromAccountId, Long toAccountId, Double amount, Long transactionId) {
        if (amount == null || amount <= 100.0) {
            return;
        }

        // 1. Get lifetime points to determine multiplier
        int lifetimePoints = rewardRepo.findAllByAccountId(fromAccountId)
                .stream()
                .mapToInt(RewardLog::getPointsEarned)
                .sum();

        double multiplier = getMultiplierForPoints(lifetimePoints);

        // 2. Base points calculation
        int basePoints = (int) Math.floor(amount / 100.0);
        if (basePoints <= 0) {
            return;
        }

        // 3. Apply multiplier
        int finalPoints = (int) Math.round(basePoints * multiplier);

        RewardLog log = new RewardLog(fromAccountId, transactionId, finalPoints, LocalDateTime.now());
        rewardRepo.save(log);
    }

    @Override
    public List<RewardLogDto> getRewardsByAccount(Long accountId) {
        return rewardRepo.findAllByAccountId(accountId)
                .stream()
                .sorted((a, b) -> b.getCreatedOn().compareTo(a.getCreatedOn()))
                .map(log -> new RewardLogDto(log.getId(), log.getTransactionId(), log.getPointsEarned(),
                        log.getCreatedOn()))
                .collect(Collectors.toList());
    }

    @Override
    public RewardSummaryDto getTotalPoints(Long accountId) {
        List<RewardLog> allLogs = rewardRepo.findAllByAccountId(accountId);

        int unredeemedTotal = allLogs.stream()
                .filter(log -> !log.isRedeemed())
                .mapToInt(RewardLog::getPointsEarned)
                .sum();

        int lifetimeTotal = allLogs.stream()
                .mapToInt(RewardLog::getPointsEarned)
                .sum();

        String tier = getTierForPoints(lifetimeTotal);
        double multiplier = getMultiplierForPoints(lifetimeTotal);
        int progress = getProgressForPoints(lifetimeTotal);

        return new RewardSummaryDto(accountId, unredeemedTotal, tier, multiplier, progress);
    }

    @Override
    @Transactional
    public RewardSummaryDto redeemPoints(Long accountId) {
        List<RewardLog> unredeemedLogs = rewardRepo.findAllByAccountIdAndIsRedeemedFalse(accountId);
        int totalPointsToRedeem = unredeemedLogs.stream().mapToInt(RewardLog::getPointsEarned).sum();

        if (totalPointsToRedeem < 100) {
            return getTotalPoints(accountId);
        }

        Optional<Account> accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setAccountBalance(account.getAccountBalance() + totalPointsToRedeem);
            accountRepo.save(account);

            for (RewardLog log : unredeemedLogs) {
                log.setRedeemed(true);
            }
            rewardRepo.saveAll(unredeemedLogs);

            return getTotalPoints(accountId);
        }

        return getTotalPoints(accountId);
    }

    private String getTierForPoints(int points) {
        if (points >= 1500)
            return "GOLD";
        if (points >= 500)
            return "SILVER";
        return "BRONZE";
    }

    private double getMultiplierForPoints(int points) {
        if (points >= 1500)
            return 1.5;
        if (points >= 500)
            return 1.2;
        return 1.0;
    }

    private int getProgressForPoints(int points) {
        if (points >= 1500)
            return 100;
        if (points >= 500) {
            // Progress towards Gold (1500) from Silver (500)
            return (int) (((points - 500) / 1000.0) * 100);
        }
        // Progress towards Silver (500) from Bronze (0)
        return (int) ((points / 500.0) * 100);
    }
}

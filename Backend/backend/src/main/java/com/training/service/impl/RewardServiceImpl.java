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
        // Eligibility check: amount must be > 100
        if (amount == null || amount <= 100.0) {
            return;
        }

        int points = (int) Math.floor(amount / 100.0);
        if (points <= 0) {
            return;
        }

        RewardLog log = new RewardLog(fromAccountId, transactionId, points, LocalDateTime.now());
        rewardRepo.save(log);
    }

    @Override
    public List<RewardLogDto> getRewardsByAccount(Long accountId) {
        return rewardRepo.findAllByAccountId(accountId)
                .stream()
                .sorted((a, b) -> b.getCreatedOn().compareTo(a.getCreatedOn()))
                .map(log -> {
                    RewardLogDto dto = new RewardLogDto(log.getId(), log.getTransactionId(), log.getPointsEarned(),
                            log.getCreatedOn());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RewardSummaryDto getTotalPoints(Long accountId) {
        int total = rewardRepo.findAllByAccountIdAndIsRedeemedFalse(accountId)
                .stream()
                .mapToInt(RewardLog::getPointsEarned)
                .sum();
        return new RewardSummaryDto(accountId, total);
    }

    @Override
    @Transactional
    public RewardSummaryDto redeemPoints(Long accountId) {
        List<RewardLog> unredeemedLogs = rewardRepo.findAllByAccountIdAndIsRedeemedFalse(accountId);
        int totalPoints = unredeemedLogs.stream().mapToInt(RewardLog::getPointsEarned).sum();

        if (totalPoints < 100) {
            return new RewardSummaryDto(accountId, totalPoints); // Not enough points
        }

        Optional<Account> accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            // 1 point = 1 rupee
            account.setAccountBalance(account.getAccountBalance() + totalPoints);
            accountRepo.save(account);

            // Mark logs as redeemed
            for (RewardLog log : unredeemedLogs) {
                log.setRedeemed(true);
            }
            rewardRepo.saveAll(unredeemedLogs);

            return new RewardSummaryDto(accountId, 0); // Points redeemed (now 0 unredeemed)
        }

        return new RewardSummaryDto(accountId, totalPoints);
    }
}

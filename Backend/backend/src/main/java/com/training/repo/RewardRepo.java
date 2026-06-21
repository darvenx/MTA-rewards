package com.training.repo;

import com.training.entities.RewardLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepo extends JpaRepository<RewardLog, Long> {
    List<RewardLog> findAllByAccountId(Long accountId);

    List<RewardLog> findAllByAccountIdAndIsRedeemedFalse(Long accountId);
}

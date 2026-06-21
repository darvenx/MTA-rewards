package com.training.service.impl;

import com.training.dto.reward.RewardLogDto;
import com.training.dto.reward.RewardSummaryDto;
import com.training.entities.RewardLog;
import com.training.repo.RewardRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("RewardServiceImpl Tests")
class RewardServiceImplTest {

    @Mock
    private RewardRepo rewardRepo;

    @InjectMocks
    private RewardServiceImpl rewardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------------------------------------------
    // calculateAndSaveReward tests
    // -------------------------------------------------------

    @Test
    @DisplayName("Eligible transfer ₹250 → 2 points saved to sender account")
    void testCalculateAndSaveReward_Eligible250() {
        rewardService.calculateAndSaveReward(1L, 2L, 250.0, 10L);

        ArgumentCaptor<RewardLog> captor = ArgumentCaptor.forClass(RewardLog.class);
        verify(rewardRepo, times(1)).save(captor.capture());
        RewardLog saved = captor.getValue();
        assertEquals(1L, saved.getAccountId());
        assertEquals(10L, saved.getTransactionId());
        assertEquals(2, saved.getPointsEarned());
    }

    @Test
    @DisplayName("Amount exactly ₹100 → not eligible, no reward saved")
    void testCalculateAndSaveReward_ExactlyOneHundred_NotEligible() {
        rewardService.calculateAndSaveReward(1L, 2L, 100.0, 11L);

        verify(rewardRepo, never()).save(any(RewardLog.class));
    }

    @Test
    @DisplayName("Amount ₹101 → eligible → 1 point saved")
    void testCalculateAndSaveReward_OneHundredAndOne() {
        rewardService.calculateAndSaveReward(1L, 2L, 101.0, 12L);

        ArgumentCaptor<RewardLog> captor = ArgumentCaptor.forClass(RewardLog.class);
        verify(rewardRepo, times(1)).save(captor.capture());
        assertEquals(1, captor.getValue().getPointsEarned());
    }

    @Test
    @DisplayName("Amount ₹350 → 3 points saved")
    void testCalculateAndSaveReward_ThreeHundredFifty() {
        rewardService.calculateAndSaveReward(1L, 2L, 350.0, 13L);

        ArgumentCaptor<RewardLog> captor = ArgumentCaptor.forClass(RewardLog.class);
        verify(rewardRepo, times(1)).save(captor.capture());
        assertEquals(3, captor.getValue().getPointsEarned());
    }

    @Test
    @DisplayName("Amount ≤ 100 (e.g. ₹99) → not eligible, no reward saved")
    void testCalculateAndSaveReward_BelowThreshold() {
        rewardService.calculateAndSaveReward(1L, 2L, 99.0, 14L);

        verify(rewardRepo, never()).save(any(RewardLog.class));
    }

    @Test
    @DisplayName("Null amount → not eligible, no reward saved")
    void testCalculateAndSaveReward_NullAmount() {
        rewardService.calculateAndSaveReward(1L, 2L, null, 15L);

        verify(rewardRepo, never()).save(any(RewardLog.class));
    }

    // -------------------------------------------------------
    // getRewardsByAccount tests
    // -------------------------------------------------------

    @Test
    @DisplayName("getRewardsByAccount returns mapped DTOs sorted newest-first")
    void testGetRewardsByAccount_ReturnsMappedDtos() {
        LocalDateTime older = LocalDateTime.now().minusDays(1);
        LocalDateTime newer = LocalDateTime.now();

        RewardLog log1 = new RewardLog(1L, 10L, 2, older);
        log1.setId(1L);
        RewardLog log2 = new RewardLog(1L, 11L, 3, newer);
        log2.setId(2L);

        when(rewardRepo.findAllByAccountId(1L)).thenReturn(Arrays.asList(log1, log2));

        List<RewardLogDto> result = rewardService.getRewardsByAccount(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        // Newest first
        assertEquals(2L, result.get(0).getRewardId());
        assertEquals(3, result.get(0).getPointsEarned());
        assertEquals(1L, result.get(1).getRewardId());
        assertEquals(2, result.get(1).getPointsEarned());

        verify(rewardRepo, times(1)).findAllByAccountId(1L);
    }

    @Test
    @DisplayName("getRewardsByAccount returns empty list when no rewards exist")
    void testGetRewardsByAccount_Empty() {
        when(rewardRepo.findAllByAccountId(99L)).thenReturn(Collections.emptyList());

        List<RewardLogDto> result = rewardService.getRewardsByAccount(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------
    // getTotalPoints tests
    // -------------------------------------------------------

    @Test
    @DisplayName("getTotalPoints sums all points for an account correctly")
    void testGetTotalPoints_SumsCorrectly() {
        RewardLog log1 = new RewardLog(1L, 10L, 2, LocalDateTime.now());
        RewardLog log2 = new RewardLog(1L, 11L, 3, LocalDateTime.now());
        RewardLog log3 = new RewardLog(1L, 12L, 5, LocalDateTime.now());

        when(rewardRepo.findAllByAccountId(1L)).thenReturn(Arrays.asList(log1, log2, log3));

        RewardSummaryDto summary = rewardService.getTotalPoints(1L);

        assertNotNull(summary);
        assertEquals(1L, summary.getAccountId());
        assertEquals(10, summary.getTotalPoints());
    }

    @Test
    @DisplayName("getTotalPoints returns 0 when no rewards exist for account")
    void testGetTotalPoints_NoRewards() {
        when(rewardRepo.findAllByAccountId(5L)).thenReturn(Collections.emptyList());

        RewardSummaryDto summary = rewardService.getTotalPoints(5L);

        assertNotNull(summary);
        assertEquals(5L, summary.getAccountId());
        assertEquals(0, summary.getTotalPoints());
    }
}

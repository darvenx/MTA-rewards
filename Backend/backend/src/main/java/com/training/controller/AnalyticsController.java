package com.training.controller;

import com.training.dto.analytics.SpendingAnalyticsDto;
import com.training.entities.Transaction;
import com.training.enums.TransactionStatus;
import com.training.repo.TransactionRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
@CrossOrigin(origins = "http://localhost:4200")
public class AnalyticsController {

    private final TransactionRepo transactionRepo;

    public AnalyticsController(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    /**
     * GET /api/v1/analytics/spending/{accountId}
     * Returns total spending (debits) grouped by category for an account.
     */
    @GetMapping("/spending/{accountId}")
    public ResponseEntity<SpendingAnalyticsDto> getSpendingAnalytics(@PathVariable Long accountId) {
        // Only count successful DEBITs from this account
        List<Transaction> debits = transactionRepo.findAllByFromAccountOrToAccount(accountId, accountId)
                .stream()
                .filter(t -> t.getFromAccount().equals(accountId)) // Is sender
                .filter(t -> t.getTransactionStatus() == TransactionStatus.SUCCESS)
                .collect(Collectors.toList());

        Map<String, Double> summary = new HashMap<>();
        int transactionCount = debits.size();
        for (Transaction t : debits) {
            String category = t.getCategory() != null ? t.getCategory().name() : "OTHER";
            summary.put(category, summary.getOrDefault(category, 0.0) + t.getAmount());
        }

        return new ResponseEntity<>(new SpendingAnalyticsDto(accountId, summary, transactionCount), HttpStatus.OK);
    }
}

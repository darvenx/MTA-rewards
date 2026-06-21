package com.training.dto.transaction;

import com.training.enums.TransactionStatus;
import java.time.LocalDateTime;

public class TransactionsDto {
    private Long transactionId;

    private Double amount;

    private String otherAccountName;

    private TransactionStatus transactionStatus;

    private String type;
    private String category;
    private LocalDateTime createdOn;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOtherAccountName() {
        return otherAccountName;
    }

    public void setOtherAccountName(String otherAccountName) {
        this.otherAccountName = otherAccountName;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
}

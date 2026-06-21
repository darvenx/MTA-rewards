package com.training.entities;

import com.training.enums.TransactionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", fromAccount=" + fromAccount +
                ", toAccount=" + toAccount +
                ", amount=" + amount +
                ", transactionStatus=" + transactionStatus +
                ", failureReason='" + failureReason + '\'' +
                ", idempotencyKey='" + idempotencyKey + '\'' +
                ", createdOn=" + createdOn +
                '}';
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    @Column
    private Long fromAccount;
    @Column
    private Long toAccount;
    @Column
    private Double amount;
    @Column
    private TransactionStatus transactionStatus;
    @Column
    private String failureReason;
    @Column
    private String idempotencyKey;
    @Column
    private LocalDateTime createdOn;

    @Column
    @Enumerated(EnumType.STRING)
    private com.training.enums.TransactionCategory category;

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Long fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Long getToAccount() {
        return toAccount;
    }

    public void setToAccount(Long toAccount) {
        this.toAccount = toAccount;
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

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public com.training.enums.TransactionCategory getCategory() {
        return category;
    }

    public void setCategory(com.training.enums.TransactionCategory category) {
        this.category = category;
    }

    public Transaction(Long transactionId, Long fromAccount, Long toAccount, Double amount,
            TransactionStatus transactionStatus, String failureReason, String idempotencyKey, LocalDateTime createdOn) {
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.transactionStatus = transactionStatus;
        this.failureReason = failureReason;
        this.idempotencyKey = idempotencyKey;
        this.createdOn = createdOn;
    }

    public Transaction() {

    }
}

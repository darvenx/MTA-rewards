package com.training.dto.transaction;

public class RecentTransactionsDto {
    private Long transactionId;
    private Long toAccount;
    private Long fromAccount;
    private Double amount;
    private String transactionStatus;

    @Override
    public String toString() {
        return "RecentTransactionsDto{" +
                "transactionId=" + transactionId +
                ", toAccount=" + toAccount +
                ", fromAccount=" + fromAccount +
                ", amount=" + amount +
                ", transactionStatus='" + transactionStatus + '\'' +
                '}';
    }

    public RecentTransactionsDto(Long transactionId, Long toAccount, Long fromAccount, Double amount, String transactionStatus) {
        this.transactionId = transactionId;
        this.toAccount = toAccount;
        this.fromAccount = fromAccount;
        this.amount = amount;
        this.transactionStatus = transactionStatus;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getToAccount() {
        return toAccount;
    }

    public void setToAccount(Long toAccount) {
        this.toAccount = toAccount;
    }

    public Long getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Long fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}

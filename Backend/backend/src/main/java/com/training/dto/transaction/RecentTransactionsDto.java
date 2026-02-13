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
}

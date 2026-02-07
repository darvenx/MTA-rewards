package com.training.dto.transaction;

import com.training.enums.TransactionStatus;

public class TransactionsDto {
    private  Long transactionId;

    private Double amount;

    private Long otherPersonAccountNumber;

    private TransactionStatus transactionStatus;

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

    public Long getOtherPersonAccountNumber() {
        return otherPersonAccountNumber;
    }

    public void setOtherPersonAccountNumber(Long otherPersonAccountNumber) {
        this.otherPersonAccountNumber = otherPersonAccountNumber;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}


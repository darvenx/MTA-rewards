package com.training.dto.transaction;

public class TransferRequestDto {
    private Long senderAccountNumber;
    private Long receiverAccountNumber;
    private String senderAccountPin;
    private String idempotencyKey;
    private Double amount;

    public Long getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(Long senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public Long getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(Long receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public String getSenderAccountPin() {
        return senderAccountPin;
    }

    public void setSenderAccountPin(String senderAccountPin) {
        this.senderAccountPin = senderAccountPin;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    private String category; // RENT, FOOD, etc.

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

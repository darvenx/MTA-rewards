package com.training.entities;


import com.training.enums.AccountStatus;
import com.training.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="accounts")
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;
    @Column
    private String accountHolderName;
    @Column
    private Double accountBalance;
    @Version
    private Integer version;
    @Column
    private LocalDateTime lastUpdated;
    @Column
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @Column
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_id")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void debit(Double amount) {
        this.accountBalance -= amount;
    }

    public void credit(Double amount) {
        this.accountBalance += amount;
    }


    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public Double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(Double accountBalance) {
        this.accountBalance = accountBalance;
    }


    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Account(Long accountId, String accountHolderName, Double accountBalance, Integer version, LocalDateTime lastUpdated, AccountType accountType, AccountStatus accountStatus,User user) {
        this.accountId = accountId;
        this.accountHolderName = accountHolderName;
        this.accountBalance = accountBalance;
        this.version = version;
        this.lastUpdated = lastUpdated;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.user = user;
    }

    public Account() {
    }
}

package com.training.service.impl;

import com.training.dto.transaction.RecentTransactionsDto;
import com.training.enums.AccountStatus;
import com.training.enums.TransactionStatus;
import com.training.enums.TransactionType;
import com.training.exceptions.*;
import com.training.dto.transaction.TransactionsDto;
import com.training.dto.transaction.TransferRequestDto;
import com.training.entities.Account;
import com.training.entities.Transaction;
import com.training.repo.AccountRepo;
import com.training.repo.TransactionRepo;
import com.training.service.RewardService;
import com.training.service.TransactionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepo transactionRepo;
    private final AccountRepo accountRepo;
    private final RewardService rewardService;

    public TransactionServiceImpl(TransactionRepo transactionRepo, AccountRepo accountRepo,
            RewardService rewardService) {
        this.transactionRepo = transactionRepo;
        this.accountRepo = accountRepo;
        this.rewardService = rewardService;
    }

    @Override
    public Boolean transferMoney(TransferRequestDto transferRequestDto)
            throws AccountNotFoundException, IncorrectPinException,
            InsufficientBalanceException, SelfTransferException, AccountLockedException {
        if (Objects.equals(transferRequestDto.getSenderAccountNumber(),
                transferRequestDto.getReceiverAccountNumber())) {
            transactionRepo.saveAndFlush(
                    new Transaction(null,
                            transferRequestDto.getSenderAccountNumber(),
                            transferRequestDto.getReceiverAccountNumber(),
                            transferRequestDto.getAmount(),
                            TransactionStatus.FAILED,
                            "can not transfer to same account",
                            null, LocalDateTime.now()));
            throw new SelfTransferException("Cant send to same account");
        }

        Optional<Account> sender = accountRepo.findById(transferRequestDto.getSenderAccountNumber());
        Optional<Account> reciever = accountRepo.findById(transferRequestDto.getReceiverAccountNumber());
        // check Exceptions
        if (sender.isEmpty() || reciever.isEmpty()) {
            transactionRepo.saveAndFlush(
                    new Transaction(null,
                            transferRequestDto.getSenderAccountNumber(),
                            transferRequestDto.getReceiverAccountNumber(),
                            transferRequestDto.getAmount(),
                            TransactionStatus.FAILED,
                            "Unidentified sender and reciever",
                            null, LocalDateTime.now()));
            throw new AccountNotFoundException();
        }
        Account senderAccount = sender.get();
        Account receiverAccount = reciever.get();
        if (senderAccount.getAccountStatus() == AccountStatus.LOCKED
                || receiverAccount.getAccountStatus() == AccountStatus.LOCKED) {
            throw new AccountLockedException();
        }
        if (senderAccount.getAccountBalance() < transferRequestDto.getAmount()) {
            transactionRepo.saveAndFlush(
                    new Transaction(null,
                            transferRequestDto.getSenderAccountNumber(),
                            transferRequestDto.getReceiverAccountNumber(),
                            transferRequestDto.getAmount(),
                            TransactionStatus.FAILED,
                            "insufficient balance",
                            null, LocalDateTime.now()));
            throw new InsufficientBalanceException();
        }
        if (!Objects.equals(senderAccount.getUser().getPassword(), transferRequestDto.getSenderAccountPin())) {
            transactionRepo.saveAndFlush(
                    new Transaction(null,
                            transferRequestDto.getSenderAccountNumber(),
                            transferRequestDto.getReceiverAccountNumber(),
                            transferRequestDto.getAmount(),
                            TransactionStatus.FAILED,
                            "incorrect pin",
                            null, LocalDateTime.now()));
            throw new IncorrectPinException();
        }

        // debit amount
        Double amount = transferRequestDto.getAmount();
        senderAccount.debit(amount);
        // credit amount
        receiverAccount.credit(amount);
        // commit
        accountRepo.save(senderAccount);
        accountRepo.save(receiverAccount);
        // add to transaction table
        Transaction transaction = new Transaction(null, senderAccount.getAccountId(), receiverAccount.getAccountId(),
                amount, TransactionStatus.SUCCESS, "", transferRequestDto.getIdempotencyKey(), LocalDateTime.now());
        transactionRepo.save(transaction);

        // award reward points to sender for eligible transactions
        rewardService.calculateAndSaveReward(
                senderAccount.getAccountId(),
                receiverAccount.getAccountId(),
                amount,
                transaction.getTransactionId());

        return true;
    }

    @Override
    public List<TransactionsDto> getTransactions(Long accountNumber)
            throws AccountNotFoundException {
        List<Transaction> txns = transactionRepo.findAllByFromAccountOrToAccount(accountNumber, accountNumber);
        List<TransactionsDto> transactions = new ArrayList<>();
        for (Transaction txn : txns) {
            // add name of person and type of transaction

            TransactionsDto tdto = new TransactionsDto();

            Long otherNumber;
            String type;
            if (Objects.equals(txn.getFromAccount(), accountNumber)) {
                otherNumber = txn.getToAccount();
                type = TransactionType.DEBIT.toString();
            } else {
                otherNumber = txn.getFromAccount();
                type = TransactionType.CREDIT.toString();
            }
            Optional<Account> otherAccount = accountRepo.findById(otherNumber);
            String otherName = otherAccount.isPresent() ? otherAccount.get().getAccountHolderName()
                    : "Unknown Account (" + otherNumber + ")";

            tdto.setOtherAccountName(otherName);
            tdto.setTransactionId(txn.getTransactionId());
            tdto.setTransactionStatus(txn.getTransactionStatus());
            tdto.setAmount(txn.getAmount());
            tdto.setType(type);
            transactions.add(tdto);
        }
        return transactions;
    }

    public List<RecentTransactionsDto> getRecent10Transactions() {
        List<Transaction> txns = transactionRepo.findAll();

        // Sort by createdOn in descending order
        txns.sort((t1, t2) -> t2.getCreatedOn().compareTo(t1.getCreatedOn()));
        System.out.println(txns);
        // Limit to 10 and map to DTOs
        List<RecentTransactionsDto> res = txns.stream()
                .limit(10)
                .map(txn -> new RecentTransactionsDto(txn.getTransactionId(), txn.getFromAccount(), txn.getToAccount(),
                        txn.getAmount(), txn.getTransactionStatus().name())) // adjust mapping as needed
                .collect(Collectors.toList());
        System.out.println("GIVING DATA BELOW");
        System.out.println(res);
        return res;
    }

}

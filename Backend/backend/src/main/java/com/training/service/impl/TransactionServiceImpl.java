package com.training.service.impl;

import com.training.exceptions.AccountNotFoundException;
import com.training.exceptions.IncorrectPinException;
import com.training.exceptions.InsufficientBalanceException;
import com.training.dto.transaction.TransactionsDto;
import com.training.dto.transaction.TransferRequestDto;
import com.training.entities.Account;
import com.training.entities.Transaction;
import com.training.repo.AccountRepo;
import com.training.repo.TransactionRepo;
import com.training.service.TransactionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepo transactionRepo;
    private final AccountRepo accountRepo;

    public TransactionServiceImpl(TransactionRepo transactionRepo, AccountRepo accountRepo) {
        this.transactionRepo = transactionRepo;
        this.accountRepo = accountRepo;
    }

    @Override
    @Transactional
    public Boolean transferMoney(TransferRequestDto transferRequestDto)
            throws AccountNotFoundException,IncorrectPinException,
            InsufficientBalanceException
    {
        Optional<Account> sender = accountRepo.findById(transferRequestDto.getSenderAccountNumber());
        Optional<Account> reciever = accountRepo.findById(transferRequestDto.getReceiverAccountNumber());
        // check Exceptions
        if(sender.isEmpty() || reciever.isEmpty()){
            System.out.println("Accounts not found");
            throw new AccountNotFoundException();
        }
        Account senderAccount = sender.get();
        Account receiverAccount = reciever.get();
        if(senderAccount.getAccountBalance() < transferRequestDto.getAmount()){
            System.out.println("Insufficient Balance Exception");
            throw new InsufficientBalanceException();
        }
//        if(!Objects.equals(senderAccount.getPin(), transferRequestDto.getSenderAccountPin())) {
//            System.out.println("Incorrect pin exception");
//            throw new IncorrectPinException();
//        }

        // debit amount
        Double amount = transferRequestDto.getAmount();
        senderAccount.debit(amount);
        // credit amount
        receiverAccount.credit(amount);
        // commit
        accountRepo.save(senderAccount);
        accountRepo.save(receiverAccount);
        return true;
    }

    @Override
    public List<TransactionsDto> getTransactions(Long accountNumber)
    throws AccountNotFoundException{
        List<Transaction> txns = transactionRepo.findAllByFromAccountOrToAccount(accountNumber,accountNumber);
        List<TransactionsDto> transactions = new ArrayList<>();
        for (Transaction txn: txns
             ) {
//            TransactionsDto tdto = TransactionsDto.builder()
//                    .transactionId(txn.getTransactionId())
//                    .transactionStatus(txn.getTransactionStatus())
//                    .amount(txn.getAmount())
//                    .build();

            TransactionsDto tdto = new TransactionsDto();
            Long otherNumber = !Objects.equals(txn.getFromAccount(), accountNumber) ? txn.getFromAccount():txn.getToAccount();
            tdto.setOtherPersonAccountNumber(otherNumber);
            transactions.add(tdto);
        }
        return transactions;
    }
}

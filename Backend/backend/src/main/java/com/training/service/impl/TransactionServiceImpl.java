package com.training.service.impl;

import com.training.enums.TransactionStatus;
import com.training.enums.TransactionType;
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

import java.time.LocalDateTime;
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
            throw new AccountNotFoundException();
        }
        Account senderAccount = sender.get();
        Account receiverAccount = reciever.get();
        if(senderAccount.getAccountBalance() < transferRequestDto.getAmount()){
            throw new InsufficientBalanceException();
        }
        if(!Objects.equals(senderAccount.getUser().getPassword(), transferRequestDto.getSenderAccountPin())){
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
        Transaction transaction = new Transaction
                (null,senderAccount.getAccountId(), receiverAccount.getAccountId()
                        ,amount, TransactionStatus.SUCCESS,""
                        ,transferRequestDto.getIdempotencyKey(), LocalDateTime.now());
        transactionRepo.save(transaction);
        return true;
    }

    @Override
    public List<TransactionsDto> getTransactions(Long accountNumber)
    throws AccountNotFoundException{
        List<Transaction> txns = transactionRepo.findAllByFromAccountOrToAccount(accountNumber,accountNumber);
        List<TransactionsDto> transactions = new ArrayList<>();
        for (Transaction txn: txns
             ) {
            // add name of person and type of transaction

            TransactionsDto tdto = new TransactionsDto();

            Long otherNumber;
            String type;
            if(Objects.equals(txn.getFromAccount(), accountNumber)){
                otherNumber = txn.getToAccount();
                type = TransactionType.DEBIT.toString();
            }
            else{
                otherNumber = txn.getFromAccount();
                type = TransactionType.CREDIT.toString();
            }
            tdto.setOtherAccountName(accountRepo.findById(otherNumber).get().getAccountHolderName());
            tdto.setTransactionId(txn.getTransactionId());
            tdto.setTransactionStatus(txn.getTransactionStatus());
            tdto.setAmount(txn.getAmount());
            tdto.setType(type);
            transactions.add(tdto);
        }
        return transactions;
    }
}

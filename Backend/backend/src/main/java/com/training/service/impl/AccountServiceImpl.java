package com.training.service.impl;

import com.training.dto.AccountDataDto;
import com.training.dto.account.AccountCreateDto;
import com.training.dto.account.AccountSuccessCreation;
import com.training.entities.Account;
import com.training.enums.AccountStatus;
import com.training.enums.AccountType;
import com.training.exceptions.AccountNotFoundException;
import com.training.exceptions.UserNotFoundException;
import com.training.repo.AccountRepo;
import com.training.repo.UserRepo;
import com.training.service.AccountService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo;
    private final UserRepo userRepo;

    public AccountServiceImpl(AccountRepo accountRepo, UserRepo userRepo) {
        this.accountRepo = accountRepo;
        this.userRepo = userRepo;
    }

    @Override
    public AccountSuccessCreation createAccount(AccountCreateDto accountCreateDto) {
        // extract data
        Account account = new Account(null,
                accountCreateDto.getAccountHolderName(),
                10000.0,null,
                LocalDateTime.now(),
                accountCreateDto.getAccountType(),
                AccountStatus.ACTIVE,
                userRepo.findById(accountCreateDto.getUserId()).get()
        );
        accountRepo.saveAndFlush(account);
        List<Account> accounts = accountRepo.findAllByUser_UserId(accountCreateDto.getUserId());
        List<AccountType> types = new ArrayList<>();
        List<AccountStatus> statuses = new ArrayList<>();
        List<Long> numbers = new ArrayList<>();
        List<Double> balances = new ArrayList<>();
        accounts.forEach(acc -> {
            types.add(acc.getAccountType());
            statuses.add(acc.getAccountStatus());
            numbers.add(acc.getAccountId());
            balances.add(acc.getAccountBalance());
        });
        AccountSuccessCreation resDto = new AccountSuccessCreation();
        resDto.setAccountType(types);
        resDto.setAccountNumbers(numbers);
        resDto.setAccountBalance(balances);
        resDto.setAccountStatus(statuses);
        return resDto;
    }

    @Override
    public Boolean isActive(Long accountNumber) {
        return accountRepo.findById(accountNumber).isPresent();
    }

    public AccountDataDto getAccountDetails(Long id) throws UserNotFoundException {
        List<Account> accounts = accountRepo.findAllByUser_UserId(id);
        if(accounts.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        AccountDataDto response = new AccountDataDto();
        for(Account account : accounts){
            response.addAccount(account.getAccountId());
            response.addBalance(account.getAccountBalance());
        }
        return response;
    }

    public AccountDataDto getAllAccountsData(Long id) {
        List<Account> accounts = accountRepo.findAllByUser_UserId(id);
        AccountDataDto res = new AccountDataDto();
        for (Account account:accounts) {
            res.addAccount(account.getAccountId());
            res.addStatus(account.getAccountStatus().name());
            res.addType(account.getAccountType().name());
            res.addBalance(account.getAccountBalance());
        }
        return res;
    }

    public Boolean toggleAccountStatus(Long id) {
        Optional<Account> accObj = accountRepo.findById(id);
        if(accObj.isEmpty()){
            throw new AccountNotFoundException();
        }
        Account acc = accObj.get();
        AccountStatus  status =  acc.getAccountStatus() == AccountStatus.LOCKED ?AccountStatus.ACTIVE : AccountStatus.LOCKED;
        acc.setAccountStatus(status);
        accountRepo.save(acc);
        return true;
    }
}
package com.training.service.impl;

import com.training.dto.user.UserLoginDto;
import com.training.dto.user.UserSignUpDto;
import com.training.dto.user.UserSuccessLoginOrSignUpDto;
import com.training.dto.user.UserUpdatePasswordDto;
import com.training.entities.Account;
import com.training.entities.User;
import com.training.exceptions.UserAlreadyExistsException;
import com.training.exceptions.UserNotFoundException;
import com.training.repo.UserRepo;
import com.training.service.UserService;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;

    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserSuccessLoginOrSignUpDto login(UserLoginDto userLoginDto)
            throws UserNotFoundException
    {
        Optional<User> userObj = userRepo.findByUsernameAndPassword(
                userLoginDto.getUsername(), userLoginDto.getPassword());
        if(userObj.isEmpty()){
            throw new UserNotFoundException();
        }
        User user = userObj.get();
        List<Account> accounts = user.getAccounts();
        List<Double> balances = new ArrayList<>();
        List<Long> accountIds = new ArrayList<>();
        accounts.forEach(account->{
            accountIds.add(account.getAccountId());
            balances.add(account.getAccountBalance());
        });
        UserSuccessLoginOrSignUpDto resDto = new UserSuccessLoginOrSignUpDto();
        resDto.setUserId(user.getUserId());
        resDto.setAccountNumbers(accountIds);
        resDto.setAccountBalance(balances);
        resDto.setToken("Random");
        return resDto;
    }

    @Override
    public Boolean updateData(UserSignUpDto userRequest)
            throws DuplicateKeyException,UserNotFoundException
    {
        Optional<User> userObj = userRepo.findByUsernameAndPassword(
                userRequest.getUsername(),userRequest.getPassword());
        if(userObj.isEmpty()){
            throw new UserNotFoundException();
        }
        User user = userObj.get();
        if(!userRequest.getEmail().isEmpty()){
            user.setEmail(userRequest.getEmail());
        }
        if(!userRequest.getPhoneNumber().isEmpty()){
            user.setPhoneNumber(userRequest.getPhoneNumber());
        }
        if(!userRequest.getFirstName().isEmpty()){
            user.setFirstName(userRequest.getFirstName());
        }
        if(!userRequest.getLastName().isEmpty()){
            user.setLastName(userRequest.getLastName());
        }
        userRepo.save(user);
        return true;
    }

    @Override
    public UserSuccessLoginOrSignUpDto singUp(UserSignUpDto userRequest)
        throws UserAlreadyExistsException
    {
        Optional<User> userObj = userRepo.findByUsernameOrEmailOrPhoneNumber(
                userRequest.getUsername(),userRequest.getEmail()
                , userRequest.getPhoneNumber());
        if(userObj.isPresent()){
            throw new UserAlreadyExistsException();
        }
        //add classes
        User user = new User(null
                ,userRequest.getPhoneNumber()
                ,userRequest.getEmail()
                ,userRequest.getUsername()
                ,userRequest.getPassword()
                ,userRequest.getFirstName()
                ,userRequest.getLastName()
                ,new ArrayList<>());

        userRepo.saveAndFlush(user);

        userObj = userRepo.findByUsernameAndPassword(
                userRequest.getUsername(), userRequest.getPassword());
        if(userObj.isPresent()){
            user = userObj.get();
        }
        UserSuccessLoginOrSignUpDto resDto = new UserSuccessLoginOrSignUpDto();
        resDto.setUserId(user.getUserId());
        resDto.setAccountNumbers(new ArrayList<>());
        resDto.setAccountBalance(new ArrayList<>());
        return resDto;
    }

    @Override
    public Boolean resetPassword(UserUpdatePasswordDto userRequest)
        throws UserNotFoundException
    {
        Optional<User> userObj = userRepo.findByUsernameAndPassword(
                userRequest.getUsername(),userRequest.getOldPassword());
        if(userObj.isEmpty()){
            throw new UserNotFoundException();
        }
        User user = userObj.get();
        user.setPassword(userRequest.getNewPassword());
        userRepo.saveAndFlush(user);
        return true;
    }
}
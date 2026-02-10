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
import com.training.service.Jwt;
import com.training.service.JwtService;
import com.training.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtService jwtService;


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
        Jwt token = jwtService.generateAccessToken(user.getUserId(), user.getUsername(),accountIds,balances,user.getRole());
        UserSuccessLoginOrSignUpDto resDto = new UserSuccessLoginOrSignUpDto();
        resDto.setToken(token.toString());
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
        Jwt token = jwtService.generateAccessToken(user.getUserId(), user.getUsername(),new ArrayList<>(),new ArrayList<>(),user.getRole());
        UserSuccessLoginOrSignUpDto resDto = new UserSuccessLoginOrSignUpDto();
        resDto.setToken(token.toString());
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
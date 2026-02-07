package com.training.service;

import com.training.dto.user.UserLoginDto;
import com.training.dto.user.UserSignUpDto;
import com.training.dto.user.UserSuccessLoginOrSignUpDto;
import com.training.dto.user.UserUpdatePasswordDto;
import com.training.exceptions.UserAlreadyExistsException;
import com.training.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

@Service
public interface UserService {

    UserSuccessLoginOrSignUpDto login(UserLoginDto userLoginDto)
        throws UserNotFoundException;

    UserSuccessLoginOrSignUpDto singUp(UserSignUpDto userRequest)
            throws UserAlreadyExistsException;

    Boolean updateData(UserSignUpDto userRequest)
            throws UserNotFoundException, DuplicateKeyException;

    Boolean resetPassword(UserUpdatePasswordDto userRequest)
            throws UserNotFoundException;
}

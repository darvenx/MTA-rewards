package com.training.controller;

import com.training.dto.user.UserLoginDto;
import com.training.dto.user.UserSignUpDto;
import com.training.dto.user.UserSuccessLoginOrSignUpDto;
import com.training.exceptions.DuplicateKeyException;
import com.training.exceptions.UserAlreadyExistsException;
import com.training.exceptions.UserNotFoundException;
import com.training.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImpl userService;

    // --- Sign Up Tests ---
    @Test
    void testSignUpSuccess() throws Exception {
        UserSignUpDto dto = new UserSignUpDto();
        UserSuccessLoginOrSignUpDto response = new UserSuccessLoginOrSignUpDto();
        when(userService.signUp(any(UserSignUpDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void testSignUpUserAlreadyExists() throws Exception {
        when(userService.signUp(any(UserSignUpDto.class)))
                .thenThrow(new UserAlreadyExistsException("User exists"));

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError()); // Exception bubbles up
    }

    // --- Login Tests ---
    @Test
    void testLoginSuccess() throws Exception {
        UserLoginDto dto = new UserLoginDto();
        UserSuccessLoginOrSignUpDto response = new UserSuccessLoginOrSignUpDto();
        when(userService.login(any(UserLoginDto.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginUserNotFound() throws Exception {
        when(userService.login(any(UserLoginDto.class)))
                .thenThrow(new UserNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    // --- Update Tests ---
    @Test
    void testUpdateSuccess() throws Exception {
        when(userService.updateData(any(UserSignUpDto.class))).thenReturn(true);

        mockMvc.perform(put("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testUpdateDuplicateKeyException() throws Exception {
        when(userService.updateData(any(UserSignUpDto.class)))
                .thenThrow(new DuplicateKeyException());

        mockMvc.perform(put("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateUserNotFoundException() throws Exception {
        when(userService.updateData(any(UserSignUpDto.class)))
                .thenThrow(new UserNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }
}


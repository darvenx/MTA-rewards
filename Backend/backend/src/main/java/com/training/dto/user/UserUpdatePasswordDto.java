package com.training.dto.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserUpdatePasswordDto {
    private String username;
    private String oldPassword;
    private String newPassword;
}

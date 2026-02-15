package com.training.dto.user;


public class UserDetailsResponseDto {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String userName;

    public UserDetailsResponseDto(String fullName, String email, String phoneNumber, String userName) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

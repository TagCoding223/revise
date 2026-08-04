package com.revise.dto.request;

public class SignupRequest {
    private String fullName;
    private String email;
    private String password;

    public SignupRequest(String password, String email, String fullName) {
        this.password = password;
        this.email = email;
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

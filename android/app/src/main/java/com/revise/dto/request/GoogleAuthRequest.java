package com.revise.dto.request;

public class GoogleAuthRequest {
    private String idToken;

    public GoogleAuthRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}

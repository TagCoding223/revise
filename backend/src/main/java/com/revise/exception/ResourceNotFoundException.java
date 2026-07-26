package com.revise.exception;

// This can be reused anywhere in your app when a database record is missing
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message){
        super(message);
    }
}

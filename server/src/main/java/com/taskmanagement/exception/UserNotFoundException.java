package com.taskmanagement.exception;

import com.taskmanagement.model.User;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(UUID userId){
        super("User with ID " + userId + " is not a Scrum Master or does not exist.");
    }
}

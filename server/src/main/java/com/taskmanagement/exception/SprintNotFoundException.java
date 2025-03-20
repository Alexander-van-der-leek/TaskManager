package com.taskmanagement.exception;

public class SprintNotFoundException extends RuntimeException{
    public SprintNotFoundException(Integer id){
        super("Sprint ID "+ id + "does not exist");
    }
}

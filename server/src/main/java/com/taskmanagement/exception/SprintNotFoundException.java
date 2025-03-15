package com.taskmanagement.exception;

import java.util.UUID;

public class SprintNotFoundException extends RuntimeException{
    public SprintNotFoundException(Integer id){
        super("Sprint ID "+ id + "does not exist");
    }
}

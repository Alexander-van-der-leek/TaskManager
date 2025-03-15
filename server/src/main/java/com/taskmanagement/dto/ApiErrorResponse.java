package com.taskmanagement.dto;

import lombok.Data;

@Data
public class ApiErrorResponse {
    private int status;
    private String message;
    private String path;
    private long timestamp;
}

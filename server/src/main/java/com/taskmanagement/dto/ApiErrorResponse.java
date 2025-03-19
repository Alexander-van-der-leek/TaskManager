package com.taskmanagement.dto;

import lombok.Data;

// specifically for api error, not auth error
@Data
public class ApiErrorResponse {
    private int status;
    private String message;
    private String path;
    private long timestamp;
}

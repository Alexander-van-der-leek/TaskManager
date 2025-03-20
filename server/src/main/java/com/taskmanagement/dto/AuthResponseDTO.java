package com.taskmanagement.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AuthResponseDTO {
    private String token;
    private UUID userId;
    private String name;
    private String email;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, UUID userId, String name, String email) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
}
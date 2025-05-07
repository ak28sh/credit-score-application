package com.user.user.response;

import lombok.Data;

@Data
public class AuthResponse {

    private String jwt;
    private Long id;
    private String username;
    private String email;
    private String message;
}

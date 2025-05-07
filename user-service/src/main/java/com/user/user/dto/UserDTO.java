package com.user.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String role;

//    public UserDTO(String username, String email, String role) {
//        this.username = username;
//        this.email = email;
//        this.role = role;
//    }


}

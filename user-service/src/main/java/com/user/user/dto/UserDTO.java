package com.user.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotNull(message = "Username must not be null")
    private String username;

    @NotNull(message = "Email must not be null")
    private String email;

    @NotNull(message = "Role must not be null")
    private String role;

//    public UserDTO(String username, String email, String role) {
//        this.username = username;
//        this.email = email;
//        this.role = role;
//    }


}

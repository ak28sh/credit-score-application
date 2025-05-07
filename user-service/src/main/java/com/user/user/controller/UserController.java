package com.user.user.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.user.user.config.JwtProvider;
import com.user.user.dto.UserDTO;
import com.user.user.entity.User;
import com.user.user.repository.UserRepository;
import com.user.user.response.AuthResponse;
import com.user.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody User user) throws Exception {

        User userExists = userRepository.findByEmail(user.getEmail());

        if(userExists != null) {
//            throw new Exception("Account already exist with given email");
            AuthResponse authResponse = new AuthResponse();
            authResponse.setMessage("Account already exist with given email");
            return new ResponseEntity<>(authResponse, HttpStatus.BAD_REQUEST);
        }

        UserDTO userDTO = userService.registerUser(user);
        if (userDTO != null) {

            Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtProvider.generateToken(authentication);

            AuthResponse authResponse = new AuthResponse();
            authResponse.setUsername(user.getUsername());
            authResponse.setEmail(user.getEmail());
            authResponse.setMessage("User Registered Successfully");
            authResponse.setJwt(jwt);

            return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestParam String username, @RequestParam String password, HttpServletRequest request) throws Exception {
        System.out.println(username + " " + password);
        UserDTO user = userService.loginUser(username, password);
        if(user != null) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), password);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtProvider.generateToken(authentication);

            AuthResponse authResponse = new AuthResponse();
            authResponse.setId(user.getId());
            authResponse.setUsername(user.getUsername());
            authResponse.setEmail(user.getEmail());
            authResponse.setMessage("User Login Successful");
            authResponse.setJwt(jwt);

            return new ResponseEntity<>(authResponse, HttpStatus.OK);
        }

        throw new Exception("Something went wrong. Check username and password");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {

        UserDTO userDTO = userService.getUserDetails(userId);
        if(userDTO != null) {
            return ResponseEntity.ok(userDTO);
        }
        return ResponseEntity.notFound().build();
    }
}

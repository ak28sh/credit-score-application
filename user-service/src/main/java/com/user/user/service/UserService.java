package com.user.user.service;

import com.user.user.dto.UserDTO;
import com.user.user.entity.User;
import com.user.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);
        return mapToDTO(user);
    }

    public UserDTO loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if(user != null && passwordEncoder.matches(password, user.getPassword()))
            return mapToDTO(user);
        return null;
    }

    public UserDTO mapToDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    public UserDTO getUserDetails(Long userId) {

        User user = userRepository.findById(userId).orElse(null);
        if  (user != null) {
            return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        }
        return null;
    }
}

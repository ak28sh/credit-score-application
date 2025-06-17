package com.creditScore.creditScore.client;

import com.creditScore.creditScore.config.JwtConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

//Declare this class as service layer to manage user related operations
@Service
public class UserManagementClient {

    //Automatically inject instance of web client to handle HTTP request
    @Autowired
    private WebClient webClient;

    //URL endpoint for user-management-service
    private final String USER_SERVICE_URL = "http://localhost:8082/users";

    //Retrieve user details from user management service using reactive web client
    public Mono<String> getuserDetails(int userId) {

        //Create an HTTP get Request
        return webClient.get()
                .uri(USER_SERVICE_URL + "/{userId}", userId)
                //Please generalize this token
                .header(JwtConstant.WEB_CLIENT_TOKEN)
                .retrieve() //Extract response body automatically
                .bodyToMono(String.class); //convert response body to Mono that emits strings
    }
}

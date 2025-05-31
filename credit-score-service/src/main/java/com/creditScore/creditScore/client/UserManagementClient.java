package com.creditScore.creditScore.client;

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
                .header("Bearer eyJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE3MzY3ODUyNjcsImV4cCI6MTczNjg3MTY2NywiZW1haWwiOiJ0b21AZ21haWwuY29tIn0.rtK6IM3zzNxEkrsC-cLNwZ61EsbcZcJSOnazvv7rg-SQ5DnUf73CQ9xLgsd_RAxalwGdUW7MFRU16fmtqKvd3A")
                .retrieve() //Extract response body automatically
                .bodyToMono(String.class); //convert response body to Mono that emits strings
    }
}

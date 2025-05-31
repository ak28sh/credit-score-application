package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

//    @Bean("userServiceWebClient")
//    public WebClient userServiceWebClient(WebClient.Builder builder) {
//        return builder
//                .baseUrl("http://localhost:8081/users")
//                .defaultHeader("Content-Type", "application/json")
//                .build();
//    }
//
//    @Bean("scoreServiceWebClient")
//    public WebClient scoreServiceWebClient(WebClient.Builder builder) {
//        return builder
//                .baseUrl("http://localhost:6464/score")
//                .defaultHeader("Content-Type", "application/json")
//                .build();
//    }

//    @Configuration
//    public class TempRestClientConfig {
//
//        @Bean
//        public RestClient.Builder restClientBuilder() {
//            return RestClient.builder();
//        }
//    }
}

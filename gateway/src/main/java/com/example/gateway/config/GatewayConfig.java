package com.example.gateway.config;


import com.example.gateway.models.CreditScore;
import com.example.gateway.models.User;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service-post", r -> r.path("/users")
                        .and().method("POST")
                        .and().readBody(User.class, s -> true)
                        .uri("http://localhost:8082"))
                .route("user-service-get", r -> r.path("/users")
                        .and().method("GET")
                        .uri("http://localhost:8082"))

                // Credit Score Service Routes
                .route("credit-score-service-post", r -> r.path("/score")
                        .and().method("POST")
                        .and().readBody(CreditScore.class, s -> true)
                        .uri("http://localhost:6464"))
                .route("credit-score-service-get", r -> r.path("/score")
                        .and().method("GET")
                        .uri("http://localhost:6464"))
                .build();
    }
}


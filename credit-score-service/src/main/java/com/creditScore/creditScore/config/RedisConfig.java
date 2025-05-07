package com.creditScore.creditScore.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

//Configure class for redis
@Configuration
public class RedisConfig {

    @Bean
    //Configure redis template to interact with redis data store using custom serializable strategies
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Set the key serializer to serialize string keys.
        template.setKeySerializer(new StringRedisSerializer());

        // Set the value serializer to serialize values as JSON.
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}

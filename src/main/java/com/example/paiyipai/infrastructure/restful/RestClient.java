package com.example.paiyipai.infrastructure.restful;

import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestClient {

    private final RestTemplate restTemplate;


    public RestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SneakyThrows
    public <T> ResponseEntity<T> post(String endpoint, Class<T> responseEntityClass) {
        return restTemplate.exchange(endpoint, HttpMethod.POST, HttpEntity.EMPTY, responseEntityClass);
    }

    public <T> ResponseEntity<T> get(String endpoint, Class<T> responseEntityClass) {
        return restTemplate.exchange(endpoint, HttpMethod.GET, HttpEntity.EMPTY, responseEntityClass);
    }
}

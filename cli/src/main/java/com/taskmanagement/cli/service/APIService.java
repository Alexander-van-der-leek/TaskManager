package com.taskmanagement.cli.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.cli.config.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class APIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final UserSession userSession;

    @Autowired
    public APIService(
            @Value("${cli.api.base-url}") String apiBaseUrl,
            UserSession userSession) {
        this.webClient = WebClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        this.objectMapper = new ObjectMapper();
        this.userSession = userSession;
    }

    public Map<String, Object> authenticate(String idToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("idToken", idToken);

        try {
            String jsonResponse = webClient.post()
                    .uri("/auth/google")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        } catch (WebClientResponseException e) {
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(
                        e.getResponseBodyAsString(), new TypeReference<Map<String, Object>>() {});
                throw new RuntimeException(String.valueOf(errorResponse.get("error")));
            } catch (JsonProcessingException jsonException) {
                throw new RuntimeException("Authentication failed: " + e.getMessage());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process server response: " + e.getMessage());
        }
    }

    public <T> T get(String uri, Class<T> responseType) {
        try {
            return webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userSession.getToken())
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException ex) {
            handleApiError(ex);
            return null;
        }
    }

    public <T> T post(String uri, Object body, Class<T> responseType) {
        try{
            return webClient.post()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userSession.getToken())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException ex) {
            handleApiError(ex);
            return null;
        }
    }

    public <T> T put(String uri, Object body, Class<T> responseType) {
        try {
            return webClient.put()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userSession.getToken())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException ex) {
            handleApiError(ex);
            return null;
        }
    }

    public <T> T patch(String uri, Object body, Class<T> responseType) {
        try {
            return webClient.patch()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userSession.getToken())
                    .bodyValue(body != null ? body : new HashMap<>())
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException ex) {
            handleApiError(ex);
            return null;
        }
    }

    public <T> T delete(String uri, Class<T> responseType) {
        try {
            return webClient.delete()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userSession.getToken())
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException ex) {
            handleApiError(ex);
            return null;
        }
    }

    private void handleApiError(WebClientResponseException ex) {
        try {
            Map<String, Object> errorResponse = objectMapper.readValue(
                    ex.getResponseBodyAsString(), new TypeReference<Map<String, Object>>() {});
            String originalMessage = (String) errorResponse.getOrDefault("message",
                    "Unknown error: " + ex.getMessage());

            String cleanedMessage = originalMessage;
            if (originalMessage.startsWith("An unexpected error occurred: ")) {
                cleanedMessage = originalMessage.substring("An unexpected error occurred: ".length());
            }

            throw new RuntimeException(cleanedMessage);
        } catch (JsonProcessingException jsonException) {
            throw new RuntimeException("API Error: " + ex.getStatusCode() + " - " + ex.getMessage());
        }
    }
}
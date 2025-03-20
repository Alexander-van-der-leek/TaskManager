package com.taskmanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.ApiErrorResponse;
import com.taskmanagement.security.JWTFilter;
import com.taskmanagement.security.JWTTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Date;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JWTTokenProvider tokenProvider;

    public SecurityConfig(JWTTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ApiErrorResponse error = new ApiErrorResponse();
                            error.setStatus(HttpStatus.UNAUTHORIZED.value());
                            error.setMessage("Unauthorized: " + authException.getMessage());
                            error.setPath(request.getRequestURI());
                            error.setTimestamp(new Date().getTime());

                            response.getWriter().write(new ObjectMapper().writeValueAsString(error));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ApiErrorResponse error = new ApiErrorResponse();
                            error.setStatus(HttpStatus.FORBIDDEN.value());
                            error.setMessage("Access denied: " + accessDeniedException.getMessage());
                            error.setPath(request.getRequestURI());
                            error.setTimestamp(new Date().getTime());

                            response.getWriter().write(new ObjectMapper().writeValueAsString(error));
                        })
                )
                .addFilterBefore(new JWTFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
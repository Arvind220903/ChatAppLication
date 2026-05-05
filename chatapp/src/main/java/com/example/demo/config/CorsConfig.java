package com.example.demo.config;

// CORS is handled centrally by SecurityConfig's CorsConfigurationSource bean.
// A separate WebMvcConfigurer CORS config conflicts with Spring Security's
// CORS filter on preflight OPTIONS requests and causes 403 on POST/PUT.
public class CorsConfig {
}

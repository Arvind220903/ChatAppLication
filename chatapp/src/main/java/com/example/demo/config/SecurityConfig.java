package com.example.demo.config;

import java.util.List;

import org.springframework.http.HttpMethod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.jwt.JwtFilter;
import com.example.demo.service.MyUserDetailService;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
	@Autowired
	private MyUserDetailService myUd;

	@Autowired
	private JwtFilter jwt;

	@Bean
	public SecurityFilterChain security(HttpSecurity http) throws Exception {
		return http
				.csrf(customizer -> customizer.disable())
				.cors(customizer -> customizer.configurationSource(corsSource()))
				.authorizeHttpRequests(request -> request
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/user/register", "/user/login", "/ws-chat/**").permitAll()
						.requestMatchers("/messages/**").authenticated()
						.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public CorsConfigurationSource corsSource() {
		CorsConfiguration config = new CorsConfiguration();
		// Include both localhost and 127.0.0.1 for both standard Angular ports
		config.setAllowedOrigins(List.of(
				"http://localhost:4200",
				"http://127.0.0.1:4200",
				"http://localhost:4201",
				"http://127.0.0.1:4201"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		// Explicitly list headers to ensure they aren't stripped
		config.setAllowedHeaders(List.of("Authorization", "Autherization", "Content-Type", "token"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public BCryptPasswordEncoder bcrypt() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authProvider() {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(myUd);
		provider.setPasswordEncoder(bcrypt());
		return provider;
	}

	@Bean
	public AuthenticationManager authManager(AuthenticationConfiguration con) throws Exception {
		return con.getAuthenticationManager();
	}
}

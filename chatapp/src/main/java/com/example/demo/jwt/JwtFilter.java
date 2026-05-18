package com.example.demo.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.MyUserDetailService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class JwtFilter extends OncePerRequestFilter {
	
	private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

	@Autowired
	private MyUserDetailService myud;
	@Autowired
	private JwtService jwt;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		// Check both standard and misspelled headers used in the app
		String header = request.getHeader("Authorization");
		if (header == null) {
			header = request.getHeader("Autherization");
		}
		
		String username = null;
		String token = null;

		if (header != null && header.startsWith("Bearer ")) {
			token = header.substring(7);
			try {
				username = jwt.extractUsername(token);
			} catch (Exception e) {
				log.warn("[JWT FILTER WARN] Token extraction failed for request to {}: {}", 
						request.getRequestURI(), e.getMessage());
			}
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				UserDetails user = myud.loadUserByUsername(username);
				if (jwt.validateToken(token, user)) {
					UsernamePasswordAuthenticationToken authToken =
							new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
					log.info("[JWT FILTER SUCCESS] Authenticated user '{}' successfully for path: {}", 
							username, request.getRequestURI());
				} else {
					log.warn("[JWT FILTER WARN] Token validation failed for user '{}' for path: {}", 
							username, request.getRequestURI());
				}
			} catch (Exception e) {
				log.error("[JWT FILTER ERROR] Exception occurred during context authentication for user '{}'", 
						username, e);
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}
}
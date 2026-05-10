package com.example.demo.jwt;

import java.io.IOException;

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
				System.out.println("DEBUG JWT FILTER: token extraction failed: " + e.getMessage());
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
				}
			} catch (Exception e) {
				System.out.println("DEBUG JWT FILTER EXCEPTION:");
				e.printStackTrace();
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}
}
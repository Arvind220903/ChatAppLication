package com.example.demo.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepo;

@Service
public class MyUserDetailService implements UserDetailsService {
	@Autowired
	private UserRepo userRepo;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		System.out.println("DEBUG loadUserByUsername: Fetching user for email: " + email);
		UserEntity user = userRepo.findByUserEmail(email);
		if (user == null) {
			System.out.println("DEBUG loadUserByUsername: User not found for email: " + email);
			throw new UsernameNotFoundException("User not found: " + email);
		}
		System.out.println("DEBUG loadUserByUsername: User found: " + user.getUserEmail() + ", Password encoded length: " + (user.getPassword() != null ? user.getPassword().length() : "null"));
		return new User(user.getUserEmail(), user.getPassword(), Collections.emptyList());
	}
}

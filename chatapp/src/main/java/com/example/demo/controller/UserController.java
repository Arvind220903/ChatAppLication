package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.NotificationEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.jwt.JwtService;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	private JwtService jwt;
	@Autowired
	private UserService userService;
	@Autowired
	private AuthenticationManager auth;

	@GetMapping("/getprofile")
	public ResponseEntity<UserEntity> getProfile(Principal principal) {
		if (principal == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.getProfile(principal.getName()));
	}

	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody UserEntity userEntity) {
		String s = userService.register(userEntity);
		if (s.equals("Success"))
			return ResponseEntity.status(HttpStatus.CREATED).body(s);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(s);
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody UserEntity user) {
		try {
			Authentication authentication = auth.authenticate(
					new UsernamePasswordAuthenticationToken(
							user.getUserEmail(), user.getPassword()));
			if (authentication.isAuthenticated())
				return ResponseEntity.ok(jwt.generateKey(user.getUserEmail()));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}
	}

	@PutMapping("/editprofile")
	public ResponseEntity<UserEntity> editprofile(@RequestBody UserEntity user) {
		UserEntity u = userService.editprofile(user);
		if (u == null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		return ResponseEntity.status(HttpStatus.OK).body(u);
	}

	@GetMapping("/followers")
	public ResponseEntity<List<UserEntity>> getFollower(Principal principal) {
		if (principal == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		return ResponseEntity.status(HttpStatus.OK).body(userService.getFollowers(principal.getName()));
	}

	@GetMapping("/followers/{email}")
	public ResponseEntity<List<UserEntity>> getFollowerByEmail(@PathVariable String email) {
		return ResponseEntity.status(HttpStatus.OK).body(userService.getFollowers(email));
	}

	@GetMapping("/followings")
	public ResponseEntity<List<UserEntity>> getFollowing(Principal principal) {
		if (principal == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		return ResponseEntity.status(HttpStatus.OK).body(userService.getFollowing(principal.getName()));
	}

	@GetMapping("/followings/{email}")
	public ResponseEntity<List<UserEntity>> getFollowingByEmail(@PathVariable String email) {
		return ResponseEntity.status(HttpStatus.OK).body(userService.getFollowing(email));
	}

	@PostMapping("/follow")
	public ResponseEntity<String> follow(Principal principal, @RequestParam int Follower) {
		if (principal == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		String s = userService.follow(principal.getName(), Follower);
		if (s.equals("Fail"))
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to Follow");
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Successfully Followed");
	}

	@GetMapping("/searchuser/{username}")
	public List<String> search(@PathVariable("username") String username) {
		return userService.search(username);
	}

	@GetMapping("/getprofilebyusername/{username}")
	public UserEntity getbyusername(Principal principal,@PathVariable String username) {
		return userService.getProfileByUserName(principal.getName(),username);
	}

	@GetMapping("/notifications")
	public List<NotificationEntity> getnotify(Principal principal) {
		if (principal == null)
			return null;
		return userService.notification(principal.getName());
	}
}

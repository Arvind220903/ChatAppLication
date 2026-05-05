package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.NotificationEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.NotificationRepo;
import com.example.demo.repository.UserRepo;
@Service
public class UserServiceImpl implements UserService{
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private NotificationRepo notificationRepo;
	
	
	@Autowired
	BCryptPasswordEncoder encode;
	@Override
	public UserEntity getProfile(String email) {
		
		UserEntity user= userRepo.findByUserEmail(email);
		user.setPostCount(user.getPosts().size());
		user.setFollowerCount(user.getFollower().size());
		user.setFollowingCount(user.getFollowing().size());
		return user;
	}

	@Override
	public String register(UserEntity user) {
		if(userRepo.findByUserEmail(user.getUserEmail())!=null )return "Email Already exists";
		if(userRepo.findByUserName(user.getUserName())!=null)return "username already taken";
		user.setPassword(encode.encode(user.getPassword()));
		userRepo.save(user);
		return "Success";
	}

	@Override
	public UserEntity editprofile(UserEntity user) {
		
		return userRepo.save(user);
	}

	@Override
	public List<UserEntity> getFollowers(String email ) {
		int userId=userRepo.findByUserEmail(email).getUserId();
		UserEntity user=userRepo.findByUserId(userId);
		List<Integer> ids=user.getFollower();
		List<UserEntity> users=new ArrayList<>();
		for(int i : ids) {
			users.add(userRepo.findByUserId(i));
		}
		return users;
	}

	@Override
	public List<UserEntity> getFollowing(String email) {
		int userId=userRepo.findByUserEmail(email).getUserId();
		UserEntity user=userRepo.findByUserId(userId);
		List<Integer> ids=user.getFollowing();
		List<UserEntity> users=new ArrayList<>();
		for(int i : ids) {
			users.add(userRepo.findByUserId(i));
		}
		return users;
	}

	@Override
	public String follow(String email, int followId) {
		UserEntity user = userRepo.findByUserEmail(email);
		int userId=user.getUserId();
		UserEntity target = userRepo.findByUserId(followId);
		if (user == null || target == null) return "Fail";

		// Initialize lists if null (first-time follow)
		List<Integer> following = user.getFollowing();
		if (following == null) following = new ArrayList<>();

		List<Integer> followers = target.getFollower();
		if (followers == null) followers = new ArrayList<>();

		if (following.contains(followId)) {
			// Unfollow
			following.remove(Integer.valueOf(followId));
			followers.remove(Integer.valueOf(userId));
		} else {
			// Follow
			following.add(followId);
			followers.add(userId);
		}
		NotificationEntity notification=new NotificationEntity();
		notification.setTitle(user.getUserName()+" started Following You");
		notification.setUserId(followId);

		user.setFollowing(following);
		target.setFollower(followers);
		notificationRepo.save(notification);
		userRepo.save(user);
		userRepo.save(target);
		return "Done";
	}

	@Override
	public List<String> search(String username) {
			List<String> l1=userRepo.getAllusername(username);
		return l1;
	}

	@Override
	public UserEntity getProfileByUserName(String username) {
		UserEntity user=userRepo.findByUserName(username);
		user.setPostCount(user.getPosts().size());
		user.setFollowerCount(user.getFollower().size());
		user.setFollowingCount(user.getFollowing().size());
		return user;
	}

	@Override
	public List<NotificationEntity> notification(String email) {
		List<NotificationEntity> posts=userRepo.findByUserEmail(email).getNotifications();
		Collections.reverse(posts);
		return posts;
	}

}

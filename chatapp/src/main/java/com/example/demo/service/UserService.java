package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.NotificationEntity;
import com.example.demo.entity.UserEntity;

@Service
public interface UserService {
	public UserEntity getProfile(String email);
	public String register(UserEntity user);
	public UserEntity editprofile(UserEntity user);
	public List<UserEntity> getFollowers(String email);
	public List<UserEntity> getFollowing(String email);
	public String follow(String email,int followr);
	public List<String> search(String username);
	public UserEntity getProfileByUserName(String username);
	public List<NotificationEntity> notification(String email);
}

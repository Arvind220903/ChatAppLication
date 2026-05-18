package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Likes;
import com.example.demo.entity.NotificationEntity;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.MessagePermissionRepo;
import com.example.demo.repository.NotificationRepo;
import com.example.demo.repository.UserRepo;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private NotificationRepo notificationRepo;
	@Autowired
	MessagePermissionRepo msgPerRepo;

	@Autowired
	BCryptPasswordEncoder encode;

	@Override
	public UserEntity getProfile(String email) {

		UserEntity user = userRepo.findByUserEmail(email);
		user.setPostCount(user.getPosts().size());
		user.setFollowerCount(user.getFollower().size());
		user.setFollowingCount(user.getFollowing().size());
		int count = 0;
		Map<Integer, Integer> map = user.getUnSeenMsg();
		if (map != null) {
			for (Integer val : map.values())
				count += val;
		}
		user.setUnSeenMsgByUser(count);
		Set<Integer> likedPost = new HashSet<>();
		Set<Integer> savedPost = new HashSet<>();

		if (user != null) {
			if (user.getLikes() != null) {
				for (Likes l : user.getLikes())
					likedPost.add(l.getPostId());
			}
			if (user.getSaved() != null) {
				for (PostEntity p : user.getSaved())
					savedPost.add(p.getPostId());
			}
		}
		List<PostEntity> posts = new ArrayList<>();
		for (PostEntity p : user.getPosts()) {
			if (likedPost.contains(p.getPostId()))
				p.setLikedByUser(true);
			if (savedPost.contains(p.getPostId()))
				p.setSaveByuser(true);
			posts.add(p);
		}

		return user;
	}

	@Override
	public String register(UserEntity user) {
		if (userRepo.findByUserEmail(user.getUserEmail()) != null)
			return "Email Already exists";
		if (userRepo.findByUserName(user.getUserName()) != null)
			return "username already taken";
		user.setPassword(encode.encode(user.getPassword()));
		userRepo.save(user);
		return "Success";
	}

	@Override
	public UserEntity editprofile(UserEntity user) {

		return userRepo.save(user);
	}

	@Override
	public List<UserEntity> getFollowers(String email) {
		int userId = userRepo.findByUserEmail(email).getUserId();
		UserEntity user = userRepo.findByUserId(userId);
		List<Integer> ids = user.getFollower();
		List<UserEntity> users = new ArrayList<>();
		for (int i : ids) {
			users.add(userRepo.findByUserId(i));
		}
		return users;
	}

	@Override
	public List<UserEntity> getFollowing(String email) {
		int userId = userRepo.findByUserEmail(email).getUserId();
		UserEntity user = userRepo.findByUserId(userId);
		List<Integer> ids = user.getFollowing();
		List<UserEntity> users = new ArrayList<>();
		for (int i : ids) {
			users.add(userRepo.findByUserId(i));
		}
		return users;
	}

	@Override
	@Transactional
	public String follow(String email, int followId) {
		UserEntity user = userRepo.findByUserEmail(email);
		int userId = user.getUserId();
		UserEntity target = userRepo.findByUserId(followId);
		if (user == null || target == null)
			return "Fail";

		// Initialize lists if null (first-time follow)
		List<Integer> following = user.getFollowing();
		if (following == null)
			following = new ArrayList<>();

		List<Integer> followers = target.getFollower();
		if (followers == null)
			followers = new ArrayList<>();

		if (following.contains(followId)) {
			// Unfollow
			following.remove(Integer.valueOf(followId));
			followers.remove(Integer.valueOf(userId));
		} else {
			// Follow
			following.add(followId);
			followers.add(userId);
		}
		NotificationEntity notification = notificationRepo.findByTitleAndUserIdAndSender(
				user.getUserName() + " started Following You", followId, user.getUserId());
		if (notification == null) {
			notification = new NotificationEntity();

			notification.setTitle(user.getUserName() + " started Following You");
			notification.setUserId(followId);
			notification.setSender(user.getUserId());

			UserEntity user1 = userRepo.findByUserId(followId);
			user1.setUnseenNoti((user1.getUnseenNoti() == null ? 0 : user1.getUnseenNoti()) + 1);
			userRepo.save(user1);
			user.setFollowing(following);
			target.setFollower(followers);
		}
		if (notification != null)
			notification.setFollowEmail(target.getUserEmail());
		notificationRepo.save(notification);
		userRepo.save(user);
		userRepo.save(target);
		return "Done";
	}

	@Override
	public List<String> search(String username) {
		List<String> l1 = userRepo.getAllusername(username);
		return l1;
	}

	@Override
	public UserEntity getProfileByUserName(String email, String username) {
		UserEntity user = userRepo.findByUserName(username);
		UserEntity currentUser = userRepo.findByUserEmail(email);
		if (user == null) return null;

		user.setPostCount(user.getPosts().size());
		user.setFollowerCount(user.getFollower().size());
		user.setFollowingCount(user.getFollowing().size());

		// Identify what the CURRENT user has liked/saved
		Set<Integer> likedPost = new HashSet<>();
		Set<Integer> savedPost = new HashSet<>();
		if (currentUser != null) {
			if (currentUser.getLikes() != null) {
				for (Likes l : currentUser.getLikes()) likedPost.add(l.getPostId());
			}
			if (currentUser.getSaved() != null) {
				for (PostEntity p : currentUser.getSaved()) savedPost.add(p.getPostId());
			}
		}

		List<PostEntity> posts = user.getPosts();
		if (posts != null) {
			for (PostEntity p : posts) {
				if (likedPost.contains(p.getPostId())) p.setLikedByUser(true);
				if (savedPost.contains(p.getPostId())) p.setSaveByuser(true);
			}
			Collections.sort(posts, (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
		}
		
		user.setPosts(posts);
		return user;
	}

	@Override
	public List<NotificationEntity> notification(String email) {
		UserEntity user1 = userRepo.findByUserEmail(email);
		user1.setUnseenNoti(0);
		userRepo.save(user1);

		List<NotificationEntity> posts = user1.getNotifications();
		Collections.reverse(posts);
		return posts;
	}

}

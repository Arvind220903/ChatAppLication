package com.example.demo.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.example.demo.entity.PostEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.PostRepo;
import com.example.demo.repository.UserRepo;

@Service
public class PostServiceImpl implements PostService{
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private PostRepo postRepo;
	
	@Override
	public List<PostEntity> getFollowing(int userId) {
		UserEntity user = userRepo.findByUserId(userId);
		if (user == null || user.getFollowing() == null || user.getFollowing().isEmpty()) {
			return new ArrayList<>();
		}
		
		Pageable topTwenty = PageRequest.of(0, 20);
		return postRepo.findByUserInOrderByPostIdDesc(user.getFollowing(), topTwenty);
	}

	@Override
	public List<PostEntity> trending() {
		// Filter posts created within the last 10 days
		Instant tenDaysAgoInstant = Instant.now().minus(10, ChronoUnit.DAYS);
		Date tenDaysAgo = Date.from(tenDaysAgoInstant);
		
		Pageable topFifteen = PageRequest.of(0, 15);
		return postRepo.findByCreatedAtAfterOrderByLikeCountDesc(tenDaysAgo, topFifteen);
	}

	@Override
	public List<PostEntity> region(int postid, double lati, double longi) {
		return null;
	}

	@Override
	public List<PostEntity> postByUser(int userId) {
		UserEntity user = userRepo.findByUserId(userId);
		if (user != null && user.getPosts() != null) {
			return user.getPosts();
		}
		return new ArrayList<>();
	}

	@Override
	public PostEntity createPost(PostEntity post,String email) {
		
		UserEntity user=userRepo.findByUserEmail(email);
		post.setUserName(user.getUserName());
		post.setUser(user.getUserId());
		user.getPosts().add(post);
		
		return postRepo.save(post);
	}

	@Override
	public String deletePost(int postid, int userId) {
		PostEntity post=postRepo.findByPostId(postid);
		if(post != null && post.getUser().equals(userId)) {
			postRepo.delete(post);
			return "deleted";
		}
		return "wrong User";
	}

	@Override
	public PostEntity editTitle(int postid, int userId, String title) {
		PostEntity post=postRepo.findByPostId(postid);
		if(post != null && post.getUser().equals(userId)) {
			post.setTitle(title);
			postRepo.save(post);
		}
		return null;
	}

	@Override
	public List<PostEntity> legacy() {
		return null;
	}

	@Override
	public List<PostEntity> feed(String email) {
		int userId=userRepo.findByUserEmail(email).getUserId();
		UserEntity user=userRepo.findByUserId(userId);
		List<PostEntity> trending = trending();
		List<PostEntity> following = getFollowing(userId);
		
		
		java.util.Set<PostEntity> mixedSet = new java.util.LinkedHashSet<>();
		
		int tIndex = 0;
		int fIndex = 0;
		
		
		while (tIndex < trending.size() || fIndex < following.size()) {
			if (fIndex < following.size()) {
				mixedSet.add(following.get(fIndex++));
			}
			if (fIndex < following.size()) {
				mixedSet.add(following.get(fIndex++));
			}
			if (tIndex < trending.size()) {
				mixedSet.add(trending.get(tIndex++));
			}
		}
	
		
		
		List<PostEntity> result = new ArrayList<>(mixedSet);
		
		return result;
	}

	

	public List<PostEntity> search(String keyword, String email) {
		int userId = userRepo.findByUserEmail(email).getUserId();
		List<PostEntity> results = postRepo.findByTitleContainingIgnoreCaseOrUserNameContainingIgnoreCase(keyword, keyword);
		
		return results;
	}

	@Override
	public boolean saved(int postId, String email) {
			PostEntity post=postRepo.findByPostId(postId);
			UserEntity user=userRepo.findByUserEmail(email);
			user.getSaved().add(post);
			return userRepo.save(user)!=null;
		
	}

	@Override
	public List<PostEntity> likePosts(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PostEntity> savedPosts(String username) {
		UserEntity user=userRepo.findByUserEmail(username);
		List<PostEntity> save=user.getSaved();
		
		return save;
	}

}

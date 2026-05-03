package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Likes;
import com.example.demo.entity.PostEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.LikesRepo;
import com.example.demo.repository.PostRepo;
import com.example.demo.repository.UserRepo;

@Service
public class LikesServiceImpl implements LikesService {
	@Autowired
	private LikesRepo likesRepo;
	@Autowired
	private PostRepo postRepo;
	@Autowired
	private UserRepo userRepo;

	@Override
	@Transactional
	public String likePost(String email, int postId) {
		UserEntity user = userRepo.findByUserEmail(email);
		Likes like = likesRepo.findByUserIdAndPostId(user.getUserId(), postId);
		if (like == null) {
			like = new Likes();
			PostEntity post=postRepo.findByPostId(postId);
			post.setLikeCount(post.getLikeCount()+1);
			post.setLikedByUser(true);
			postRepo.save(post);
			like.setUserId(user.getUserId());
			like.setPostId(postId);
			likesRepo.save(like);
			
			return "liked";
		} else {
			// Must clear the join table (users_likes) BEFORE deleting from likes
			PostEntity post=postRepo.findByPostId(postId);
			post.setLikeCount(post.getLikeCount()-1);
			post.setLikedByUser(false);
			postRepo.save(post);
			likesRepo.deleteFromJoinTable(like.getLikeId());
			likesRepo.deleteByUserIdAndPostId(user.getUserId(), postId);
			return "unliked";
		}
	}

	@Override
	public int likeCount(int postId) {
		PostEntity post = postRepo.findByPostId(postId);
		if (post == null) return 0;
		return post.getLikeCount();
	}

	@Override
	public List<PostEntity> getByLikes(String username) {
		UserEntity user = userRepo.findByUserEmail(username);
		if (user == null) return new ArrayList<>();
		
		List<Likes> likes = likesRepo.findByUserId(user.getUserId());
		List<PostEntity> ans = new ArrayList<>();
		for(Likes like : likes) {
			PostEntity post = postRepo.findByPostId(like.getPostId());
			if (post != null) {
				
				ans.add(post);
			}
		}
		return ans;
	}
}

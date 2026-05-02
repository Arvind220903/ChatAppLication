package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Likes;
import com.example.demo.entity.PostEntity;
import com.example.demo.repository.LikesRepo;
import com.example.demo.repository.PostRepo;

@Service
public class LikesServiceImpl implements LikesService {
	@Autowired
	private LikesRepo likesRepo;
	@Autowired
	private PostRepo postRepo;

	@Override
	public String likePost(int userId, int postId) {
		PostEntity post = postRepo.findByPostId(postId);
		if (post == null) return "Post not found";

		// Simple implementation: prevent duplicate likes by checking if user already liked
		List<Likes> existingLikes = post.getLikes();
		if (existingLikes != null) {
			for (Likes l : existingLikes) {
				if (l.getUserId() == userId) {
					// Toggle unlike if already liked
					likesRepo.delete(l);
					post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
					postRepo.save(post);
					return "unliked";
				}
			}
		}

		// Add new like
		Likes like = new Likes();
		like.setPostId(postId);
		like.setUserId(userId);
		likesRepo.save(like);

		post.setLikeCount(post.getLikeCount() + 1);
		postRepo.save(post);

		return "liked";
	}

	@Override
	public int likeCount(int postId) {
		PostEntity post = postRepo.findByPostId(postId);
		if (post == null) return 0;
		return post.getLikeCount();
	}
}

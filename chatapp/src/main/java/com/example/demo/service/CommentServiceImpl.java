package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.CommentEntity;
import com.example.demo.entity.NotificationEntity;
import com.example.demo.entity.PostEntity;
import com.example.demo.repository.CommentRepo;
import com.example.demo.repository.NotificationRepo;
import com.example.demo.repository.PostRepo;
import com.example.demo.repository.UserRepo;

@Service
public class CommentServiceImpl implements CommentService{
	@Autowired
	private CommentRepo commentRepo;
	@Autowired
	private PostRepo postRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private NotificationRepo notificationRepo;

	@Override
	public List<CommentEntity> addComment(CommentEntity comment) {
		PostEntity post = postRepo.findByPostId(comment.getPostId());
		if (post == null) return null;
		comment.setUsername(userRepo.findByUserId(comment.getUserId()).getUserName());
		CommentEntity saved = commentRepo.save(comment);
		post.getComments().add(saved);
		postRepo.save(post);
		NotificationEntity note=new NotificationEntity();
		note.setTitle(saved.getUsername()+" comment on your Post");
		note.setUserId(post.getUser());
		notificationRepo.save(note);
		return post.getComments();
	}

	@Override
	public String editComment(int commentId, int userid, String change) {
		CommentEntity comment = commentRepo.findByCommentId(commentId);
		if (comment != null && comment.getUserId().equals(userid)) {
			comment.setComment(change);
			commentRepo.save(comment);
			return "Success";
		}
		return "Failed";
	}

	@Override
	public String deleteComment(int commentId, int userid) {
		CommentEntity comment = commentRepo.findByCommentId(commentId);
		if (comment != null && comment.getUserId().equals(userid)) {
			commentRepo.delete(comment);
			return "Deleted";
		}
		return "Failed";
	}

	@Override
	public List<PostEntity> getPostsByComments(String username) {
		// Query the comment table directly by userId — the UserEntity.comment
		// join table is never populated so getComment() always returns empty.
		com.example.demo.entity.UserEntity user = userRepo.findByUserEmail(username);
		if (user == null) return new ArrayList<>();

		List<CommentEntity> comments = commentRepo.findByUserId(user.getUserId());
		List<PostEntity> ans = new ArrayList<>();
		java.util.Set<Integer> seen = new java.util.LinkedHashSet<>();

		for (CommentEntity c : comments) {
			if (seen.add(c.getPostId())) {          // deduplicate — same post, multiple comments
				PostEntity post = postRepo.findByPostId(c.getPostId());
				if (post != null) ans.add(post);
			}
		}
		Collections.reverse(ans);                   // newest first
		return ans;
	}
}

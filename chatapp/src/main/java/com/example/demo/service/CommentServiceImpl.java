package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.CommentEntity;
import com.example.demo.entity.PostEntity;
import com.example.demo.repository.CommentRepo;
import com.example.demo.repository.PostRepo;

@Service
public class CommentServiceImpl implements CommentService{
	@Autowired
	private CommentRepo commentRepo;
	@Autowired
	private PostRepo postRepo;

	@Override
	public List<CommentEntity> addComment(CommentEntity comment) {
		PostEntity post = postRepo.findByPostId(comment.getPostId());
		if (post == null) return null;
		
		CommentEntity saved = commentRepo.save(comment);
		post.getComments().add(saved);
		postRepo.save(post);
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
}

package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public interface LikesService {
	public String likePost(int userId,int postId);
	public int likeCount(int commentId);
}

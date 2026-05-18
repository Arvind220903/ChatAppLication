package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.PostEntity;

@Service
public interface LikesService {
	public String likePost(String email,int postId);
	public int likeCount(int commentId);
	public List<PostEntity> getByLikes(String username,int pageNumber,int pageSize);
	
}

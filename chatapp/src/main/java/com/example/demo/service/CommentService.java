package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.CommentEntity;

@Service
public interface CommentService {
	public List<CommentEntity> addComment(CommentEntity comment);
	public String editComment(int commentId,int userid,String comment);
	public String deleteComment(int commentId,int userid);
	
}

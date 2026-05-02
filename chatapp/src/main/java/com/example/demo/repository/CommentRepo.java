package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.CommentEntity;

@Repository
public interface CommentRepo extends JpaRepository<CommentEntity,Integer>{

	


	CommentEntity findByUserId(int userid);
	CommentEntity findByCommentId(int commentId);
}

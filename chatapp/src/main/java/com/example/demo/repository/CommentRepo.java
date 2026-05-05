package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.CommentEntity;

@Repository
public interface CommentRepo extends JpaRepository<CommentEntity,Integer>{

	List<CommentEntity> findByUserId(int userid);
	CommentEntity findByCommentId(int commentId);
}

package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Likes;
import com.example.demo.entity.PostEntity;

@Repository
public interface LikesRepo extends JpaRepository<Likes, Integer> {
	List<Likes> findByUserId(int userId);

	Likes findByUserIdAndPostId(int userId, int postId);

	// Step 1: remove from join table users_likes
	@Modifying
	@Query(value = "DELETE FROM users_likes WHERE likes_like_id = :likeId", nativeQuery = true)
	void deleteFromJoinTable(@Param("likeId") int likeId);

	// Step 2: delete the likes row itself
	@Modifying
	@Query("DELETE FROM Likes l WHERE l.userId = :userId AND l.postId = :postId")
	void deleteByUserIdAndPostId(@Param("userId") int userId, @Param("postId") int postId);

	List<PostEntity> findByPostId(int postId);
}

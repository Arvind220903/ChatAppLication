package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.example.demo.entity.PostEntity;

@Repository

public interface PostRepo extends JpaRepository<PostEntity, Integer> {

	PostEntity findByPostId(int postId);

	List<PostEntity> findByTitleContainingIgnoreCaseOrUserNameContainingIgnoreCase(String title, String userName);

	List<PostEntity> findByUserInOrderByPostIdDesc(List<Integer> userIds, Pageable pageable);

	List<PostEntity> findByCreatedAtAfterOrderByLikeCountDesc(LocalDateTime tenDaysAgo, Pageable pageable);
	
	List<PostEntity> findByLatitudeBetweenAndLongitudeBetween(double minLat, double maxLat, double minLng, double maxLng);
}

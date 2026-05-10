package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.NotificationEntity;

@Repository
public interface NotificationRepo extends JpaRepository<NotificationEntity,Integer>{

	NotificationEntity findByTitle(String string);

	NotificationEntity findByTitleAndUserIdAndPostIdAndSender(String string, Integer user, Integer postId,
			Integer userId);

	NotificationEntity findByTitleAndUserIdAndSender(String string, int followId, Integer userId);

}

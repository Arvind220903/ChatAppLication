package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.MessageEntity;

@Repository
public interface MessageRepo extends JpaRepository<MessageEntity,Integer>{

	List<MessageEntity> findBySender(Integer userId);

	Collection<? extends MessageEntity> findByReceiver(Integer userId);

	@org.springframework.data.jpa.repository.Query("SELECT m FROM MessageEntity m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
	List<MessageEntity> findDirectMessages(@org.springframework.data.repository.query.Param("user1") Integer user1, @org.springframework.data.repository.query.Param("user2") Integer user2);

}

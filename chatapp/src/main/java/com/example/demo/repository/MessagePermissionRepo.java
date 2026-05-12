package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.MessagePermission;

@Repository
public interface MessagePermissionRepo extends JpaRepository<MessagePermission,Integer>{

	MessagePermission findByUserId1AndUserId2(int userId1, int userId2);

	List<MessagePermission> findByUserId2AndStatus(int userId, String string);

}

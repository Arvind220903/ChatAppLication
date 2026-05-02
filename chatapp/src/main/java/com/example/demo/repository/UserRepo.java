package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.UserEntity;

@Repository

public interface UserRepo extends JpaRepository<UserEntity, Integer>{

	UserEntity findByUserEmail(String email);

	UserEntity findByUserId(int userId);
	@Query("select u.userName from UserEntity u where u.userName like concat('%', :keyword, '%')")
	List<String> getAllusername(@Param("keyword") String keyword);

}

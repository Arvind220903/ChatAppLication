package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Likes;

@Repository
public interface LikesRepo extends JpaRepository<Likes, Long>{

}

package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.MessageEntity;

@Repository
public interface MessageRepo extends JpaRepository<MessageEntity,Integer>{

}

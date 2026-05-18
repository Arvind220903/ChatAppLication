package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.MessagePermission;

@Service
public interface MessagePermissionService {
	public String changePermission(int userId1,int userId2,String message);
	public List<MessagePermission> getRequests(int userId);
}

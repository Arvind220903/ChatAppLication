package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.MessagePermission;
import com.example.demo.repository.MessagePermissionRepo;

@Service
public class MessagePermissionImpl implements MessagePermissionService{
	@Autowired
	private MessagePermissionRepo msgPerRepo;
	@Override
	public String changePermission(int userId1, int userId2, String message) {
		MessagePermission msg=msgPerRepo.findByUserId1AndUserId2(userId1,userId2);
		if(msg==null) {
			msg=new MessagePermission();
			msg.setUserId1(userId1);
			msg.setUserId2(userId2);
		}
		msg.setStatus(message);
		if(message.equals("approve")) {
			MessagePermission msg1=msgPerRepo.findByUserId1AndUserId2(userId2,userId1);
			if(msg1==null) {
				msg1=new MessagePermission();
				msg1.setUserId1(userId2);
				msg1.setUserId2(userId1);
			}
			msg1.setStatus(message);
			msgPerRepo.save(msg1);
			
		}
		msgPerRepo.save(msg);
		
		
		return "Success";
	}
	@Override
	public List<MessagePermission> getRequests(int userId) {
		List<MessagePermission> request=msgPerRepo.findByUserId2AndStatus(userId,"Requested");
		return request;
	}

}

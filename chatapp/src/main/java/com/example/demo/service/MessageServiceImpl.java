package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.MessageRepo;
import com.example.demo.repository.UserRepo;

@Service
public class MessageServiceImpl implements MessageService{
	@Autowired
	private MessageRepo messageRepo;
	@Autowired
	private UserRepo userRepo;
	@Override
	public String sendMessage(String userEmail, int reciever,String msg) {
		MessageEntity message=new MessageEntity();
		UserEntity sender=userRepo.findByUserEmail(userEmail);
		UserEntity r=userRepo.findByUserId(reciever);
	
		if(sender==null || r==null) {
			if(sender==null)return "User Need to Login again";
			if(r==null)return "reciever does not exist";
			else {
				return "both not exist";
			}
		}
		message.setSender(sender.getUserId());
		message.setReceiver(r.getUserId());
		message.setMsg(msg);
		message=messageRepo.save(message);
		if(message==null)return "error";
		List<MessageEntity> messages=sender.getMessages();
		messages.add(message);
		sender.setMessages(messages);
		if(!sender.getChatWith().contains(r)) {
			List<UserEntity> chats=sender.getChatWith();
			if(chats==null)chats=new ArrayList<>();
			chats.add(r);
			sender.setChatWith(chats);
			List<UserEntity> rec=r.getChatWith();
			if(chats==null)rec=new ArrayList<>();
			rec.add(sender);
			r.setChatWith(rec);
		}
		userRepo.save(sender);
		userRepo.save(r);
		
		return "Ok";
	}

	@Override
	public List<MessageEntity> getMessages(String email, int userId) {
		List<MessageEntity> ans=new ArrayList<>();
		UserEntity sender=userRepo.findByUserEmail(email);
		UserEntity r=userRepo.findByUserId(userId);
		List<MessageEntity> senderMessages=sender.getMessages();
		List<MessageEntity> recieverMessages=r.getMessages();
		if(senderMessages!=null) {
			for(MessageEntity m:senderMessages) {
				ans.add(m);
			}
		}
		if(recieverMessages!=null) {
			for(MessageEntity m:recieverMessages) {
				ans.add(m);
			}
		}
		Collections.sort(ans,(a,b)->b.getCreatedAt().compareTo(a.getCreatedAt()));
		
		return ans;
	}

	@Override
	public List<UserEntity> chatWith(String email) {
		
		return userRepo.findByUserEmail(email).getChatWith();
	}

}

package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.UserEntity;

@Service
public interface MessageService {
	public MessageEntity sendMessage(int userId,int reciever,String msg);
	public List<MessageEntity> getMessages(String email,int userId);
	public List<UserEntity> chatWith(String email);
	public MessageEntity save(MessageEntity message);
}

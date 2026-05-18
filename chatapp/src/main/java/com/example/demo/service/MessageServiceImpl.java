package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.MessageRepo;
import com.example.demo.repository.UserRepo;

@Service
public class MessageServiceImpl implements MessageService {
	@Autowired
	private MessageRepo messageRepo;
	@Autowired
	private UserRepo userRepo;

	@Override
	@org.springframework.transaction.annotation.Transactional
	public MessageEntity sendMessage(int userId, int reciever, String msg) {
		MessageEntity message = new MessageEntity();
		UserEntity sender = userRepo.findByUserId(userId);
		UserEntity r = userRepo.findByUserId(reciever);

		if (sender == null || r == null) {
			return null;
		}
		message.setSender(sender.getUserId());
		message.setReceiver(r.getUserId());
		message.setMsg(msg);
		message = messageRepo.save(message);
		if (message == null)
			return null;
		List<MessageEntity> messages = sender.getMessages();
		messages.add(message);
		sender.setMessages(messages);
		if (!sender.getChatWith().contains(r)) {
			List<UserEntity> chats = sender.getChatWith();
			if (chats == null)
				chats = new ArrayList<>();
			chats.add(r);
			sender.setChatWith(chats);
			List<UserEntity> rec = r.getChatWith();
			if (chats == null)
				rec = new ArrayList<>();
			rec.add(sender);
			r.setChatWith(rec);
		}
		// Update Sender's Last Messages list
		List<MessageEntity> senderLast = new ArrayList<>(sender.getLastMessages());
		senderLast.removeIf(m -> m.getReceiver() == r.getUserId() || m.getSender() == r.getUserId());
		senderLast.add(0, message);
		sender.setLastMessages(senderLast);

		// Update Receiver's Last Messages list
		List<MessageEntity> receiverLast = new ArrayList<>(r.getLastMessages());
		receiverLast.removeIf(m -> m.getReceiver() == sender.getUserId() || m.getSender() == sender.getUserId());
		receiverLast.add(0, message);
		r.setLastMessages(receiverLast);
		List<UserEntity> chats=new ArrayList<>();
		chats.addAll(sender.getChatWith());
		chats.remove(r);
		chats.add(0,r);
		r.setChatWith(chats);
		userRepo.save(sender);
		chats=new ArrayList<>();
		chats.addAll(r.getChatWith());
		chats.remove(sender);
		chats.add(0,sender);
		sender.setChatWith(chats);
		Map<Integer,Integer> map=r.getUnSeenMsg();
		if(map==null)map=new HashMap<>();
		if(map.containsKey(sender.getUserId()))map.put(sender.getUserId(), map.get(sender.getUserId()) + 1);
		else {
			map.put(sender.getUserId(), 1);
		}
		r.setUnSeenMsg(map);
		userRepo.save(r);
		

		return message;
	}

	@Override
	@org.springframework.transaction.annotation.Transactional
	public List<MessageEntity> getMessages(String email, int userId) {
		UserEntity sender = userRepo.findByUserEmail(email);
		UserEntity r = userRepo.findByUserId(userId);
		if (sender == null || r == null) return new java.util.ArrayList<>();

		// Fetch messages between two users directly and sorted from the database!
		List<MessageEntity> ans = messageRepo.findDirectMessages(sender.getUserId(), r.getUserId());

		Map<Integer,Integer> map=sender.getUnSeenMsg();
		if(map==null)map=new java.util.HashMap<>();
		map.put(r.getUserId(), 0);
		sender.setUnSeenMsg(map);
		userRepo.save(sender);

		return ans;
	}

	

	@Override
	@org.springframework.transaction.annotation.Transactional
	public List<UserEntity> chatWith(String email) {
		UserEntity user1=userRepo.findByUserEmail(email);
		if (user1 == null) return new ArrayList<>();

		List<UserEntity> users = user1.getChatWith();
		List<UserEntity> result=new ArrayList<>();
		List<MessageEntity> lastMessages=user1.getLastMessages();
		Map<Integer,Integer> map=user1.getUnSeenMsg();
		if(map==null)map=new HashMap<>();
				
		for(UserEntity user : users){
			for(MessageEntity m:lastMessages) {
				if(m.getSender()==user.getUserId() || m.getReceiver()==user.getUserId()) {
					user.setLastmsg(m.getCreatedAt());
					if(map.containsKey(user.getUserId()))user.setUnSeenMsgByUser(map.get(user.getUserId()));
					result.add(user);
					break;
				}
			}
		}
		result.remove(user1);
		return result;
	}

	@Override
	public MessageEntity save(MessageEntity message) {
		return messageRepo.save(message);
		
	}
	

}

package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.MessageRepo;
import com.example.demo.repository.UserRepo;

@Service
public class MessageServiceImpl implements MessageService {
	
	private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

	@Autowired
	private MessageRepo messageRepo;
	@Autowired
	private UserRepo userRepo;

	@Override
	@org.springframework.transaction.annotation.Transactional
	public MessageEntity sendMessage(int userId, int reciever, String msg) {
		log.info("[MESSAGE TX] Initializing message delivery - Sender ID: {}, Receiver ID: {}", userId, reciever);
		
		MessageEntity message = new MessageEntity();
		UserEntity sender = userRepo.findByUserId(userId);
		UserEntity r = userRepo.findByUserId(reciever);

		if (sender == null || r == null) {
			log.warn("[MESSAGE TX ERROR] Delivery aborted. Sender (ID: {}) or Receiver (ID: {}) not found in database!", 
					userId, reciever);
			return null;
		}
		
		message.setSender(sender.getUserId());
		message.setReceiver(r.getUserId());
		message.setMsg(msg);
		message = messageRepo.save(message);
		
		if (message == null) {
			log.error("[MESSAGE TX ERROR] Database failed to persist message from Sender ID: {} to Receiver ID: {}", 
					userId, reciever);
			return null;
		}
		log.info("[MESSAGE TX DB] Successfully stored message in DB. Assigned Message ID: {}", message.getMsgId());
		
		// Establish contact link if first time chatting
		Set<UserEntity> senderChats = sender.getChatWith();
		if (senderChats == null) senderChats = new HashSet<>();
		
		Set<UserEntity> receiverChats = r.getChatWith();
		if (receiverChats == null) receiverChats = new HashSet<>();

		if (!senderChats.contains(r)) {
			log.info("[MESSAGE TX LINK] First chat event. Creating chat contacts link between User ID: {} and User ID: {}", 
					sender.getUserId(), r.getUserId());
			senderChats.add(r);
			sender.setChatWith(senderChats);
			
			receiverChats.add(sender);
			r.setChatWith(receiverChats);
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
		
		userRepo.save(sender);
		
		// Manage Unread Message Counters
		Map<Integer, Integer> map = r.getUnSeenMsg();
		if (map == null) map = new HashMap<>();
		int count = 1;
		if (map.containsKey(sender.getUserId())) {
			count = map.get(sender.getUserId()) + 1;
		}
		map.put(sender.getUserId(), count);
		r.setUnSeenMsg(map);
		
		log.info("[MESSAGE TX UNSEEN] Incrementing unseen counter. Sender ID: {} has {} unseen messages for Receiver ID: {}", 
				sender.getUserId(), count, r.getUserId());
		
		userRepo.save(r);
		
		log.info("[MESSAGE TX SUCCESS] Message ID: {} transaction committed successfully.", message.getMsgId());
		return message;
	}

	@Override
	@org.springframework.transaction.annotation.Transactional
	public List<MessageEntity> getMessages(String email, int userId) {
		log.info("[MESSAGE SERVICE] Loading chat history for user Email: '{}' with contact ID: #{}", email, userId);
		
		UserEntity sender = userRepo.findByUserEmail(email);
		UserEntity r = userRepo.findByUserId(userId);
		if (sender == null || r == null) {
			log.warn("[MESSAGE SERVICE WARN] Failed to load chat history. User Email: '{}' or Target ID: #{} not found.", 
					email, userId);
			return new java.util.ArrayList<>();
		}

		
		List<MessageEntity> ans = messageRepo.findDirectMessages(sender.getUserId(), r.getUserId());
		Map<Integer, Integer> map = sender.getUnSeenMsg();
		if (map == null) map = new java.util.HashMap<>();
		map.put(r.getUserId(), 0);
		sender.setUnSeenMsg(map);
		userRepo.save(sender);

		log.info("[MESSAGE SERVICE] Successfully fetched {} messages between User ID: #{} and User ID: #{} and cleared unseen count.", 
				(ans != null ? ans.size() : 0), sender.getUserId(), r.getUserId());
		return ans;
	}

	@Override
	@org.springframework.transaction.annotation.Transactional
	public List<UserEntity> chatWith(String email) {
		log.info("[CONTACT SERVICE] Loading active chat contacts for user Email: '{}'", email);
		
		UserEntity user1 = userRepo.findByUserEmail(email);
		if (user1 == null) {
			log.warn("[CONTACT SERVICE WARN] Active user not found for Email: '{}'", email);
			return new ArrayList<>();
		}

		Set<UserEntity> users = user1.getChatWith();
		List<UserEntity> result = new ArrayList<>();
		List<MessageEntity> lastMessages = user1.getLastMessages();
		Map<Integer, Integer> map = user1.getUnSeenMsg();
		if (map == null) map = new HashMap<>();
				
		for (UserEntity user : users) {
			for (MessageEntity m : lastMessages) {
				if (m.getSender() == user.getUserId() || m.getReceiver() == user.getUserId()) {
					user.setLastmsg(m.getCreatedAt());
					if (map.containsKey(user.getUserId())) {
						user.setUnSeenMsgByUser(map.get(user.getUserId()));
					}
					result.add(user);
					break;
				}
			}
		}
		result.remove(user1);
		
		log.info("[CONTACT SERVICE] Found {} active contacts for user Email: '{}'", result.size(), email);
		return result;
	}

	@Override
	public MessageEntity save(MessageEntity message) {
		log.debug("[MESSAGE SERVICE] Direct save called for Message Entity.");
		return messageRepo.save(message);
	}
}

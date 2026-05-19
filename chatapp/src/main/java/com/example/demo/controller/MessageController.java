package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.service.MessageService;
import com.example.demo.repository.UserRepo;

@RestController
@RequestMapping("/messages")
public class MessageController {
	
    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;
    @Autowired
    private SimpMessagingTemplate  smt;
    @Autowired
    private UserRepo userRepo;

    @MessageMapping("/ws-chat")
    public void sendMessage(@Payload MessageEntity msg) {
        log.info("[CHAT INSTANT] Inbound message over WebSocket - Sender ID: {}, Receiver ID: {}, Length: {} chars", 
                msg.getSender(), msg.getReceiver(), (msg.getMsg() != null ? msg.getMsg().length() : 0));
        
        MessageEntity save = messageService.sendMessage(msg.getSender(), msg.getReceiver(), msg.getMsg());
        if (save != null) {
            // Push to recipient's private topic
            smt.convertAndSend("/topic/messages/" + save.getReceiver(), save);
            // Push back to sender's private topic to sync across multiple open devices/tabs
            smt.convertAndSend("/topic/messages/" + save.getSender(), save);
            log.info("[CHAT ROUTE] Successfully stored & dispatched Message ID: {} to channels /topic/messages/{} and /topic/messages/{}", 
                    save.getMsgId(), save.getReceiver(), save.getSender());
        } else {
            log.warn("[CHAT WARNING] Failed to persist and dispatch message from Sender: {} to Receiver: {}. Entity save returned null.", 
                    msg.getSender(), msg.getReceiver());
        }
    }

    @GetMapping("/history/{userId}")
    public List<MessageEntity> getChatHistory(Principal principal, @PathVariable("userId") int userId) {
        if (principal == null) {
            log.warn("[CHAT SECURE] Rejected history fetch request. Principal is null.");
            return null;
        }
        log.info("[CHAT HISTORY] History fetch requested by User: '{}' for target User ID: #{}", 
                principal.getName(), userId);
        
        List<MessageEntity> history = messageService.getMessages(principal.getName(), userId);
        log.info("[CHAT HISTORY] Retrieved {} messages for conversation between '{}' and User ID: #{}", 
                (history != null ? history.size() : 0), principal.getName(), userId);
        return history;
    }

    @GetMapping("/contacts")
    public List<UserEntity> getChatContacts(Principal principal) {
        if (principal == null) {
            log.warn("[CHAT SECURE] Rejected contacts fetch request. Principal is null.");
            return null;
        }
        log.info("[CHAT CONTACTS] Contacts list requested by User: '{}'", principal.getName());
        
        List<UserEntity> contacts = messageService.chatWith(principal.getName());
        log.info("[CHAT CONTACTS] Found {} active contacts for User: '{}'", 
                (contacts != null ? contacts.size() : 0), principal.getName());
        return contacts;
    }
  
}

package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.service.MessageService;
import com.example.demo.repository.UserRepo;

@RestController
@RequestMapping("/messages")
public class MessageController {
	
    @Autowired
    private MessageService messageService;
    @Autowired
    private SimpMessagingTemplate  smt;
    @Autowired
    private UserRepo userRepo;

    @MessageMapping("/ws-chat")
    public void sendMessage(@Payload MessageEntity msg) {
        MessageEntity save=messageService.sendMessage(msg.getSender(), msg.getReceiver(), msg.getMsg());
        if (save != null) {
            // Push to recipient's private topic
            smt.convertAndSend("/topic/messages/" + save.getReceiver(), save);
            // Push back to sender's private topic to sync across multiple open devices/tabs
            smt.convertAndSend("/topic/messages/" + save.getSender(), save);
        }
    }

    @GetMapping("/history/{userId}")
    public List<MessageEntity> getChatHistory(Principal principal, @PathVariable("userId") int userId) {
        if (principal == null) return null;
        return messageService.getMessages(principal.getName(), userId);
    }

    @GetMapping("/contacts")
    public List<UserEntity> getChatContacts(Principal principal) {
        if (principal == null) return null;
        return messageService.chatWith(principal.getName());
    }
  
}

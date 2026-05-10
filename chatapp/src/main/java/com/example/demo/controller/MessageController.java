package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.service.MessageService;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send/{receiverId}")
    public String sendMessage(Principal principal, @PathVariable("receiverId") int receiverId, @RequestBody String msg) {
        if (principal == null) return "User not authenticated";
        return messageService.sendMessage(principal.getName(), receiverId, msg);
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

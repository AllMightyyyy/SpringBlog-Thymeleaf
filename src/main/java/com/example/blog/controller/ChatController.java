package com.example.blog.controller;

import com.example.blog.model.ChatMessage;
import com.example.blog.model.MessageType;
import com.example.blog.model.User;
import com.example.blog.repository.UserRepository;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        // Get the authenticated user's display name
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String displayName = user.getUserProfile() != null ?
                user.getUserProfile().getDisplayName() : email;

        // Set the correct sender name
        chatMessage.setSender(displayName);

        if (chatMessage.getRoom() != null && !chatMessage.getRoom().trim().isEmpty()) {
            messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoom(), chatMessage);
        } else if (chatMessage.getRecipient() != null && !chatMessage.getRecipient().trim().isEmpty()) {
            messagingTemplate.convertAndSendToUser(chatMessage.getRecipient(), "/queue/messages", chatMessage);
        } else {
            messagingTemplate.convertAndSend("/topic/global", chatMessage);
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage,
                        @Header("simpSessionId") String sessionId,
                        Principal principal) {
        // Get the authenticated user's display name
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String displayName = user.getUserProfile() != null ?
                user.getUserProfile().getDisplayName() : email;

        chatMessage.setType(MessageType.JOIN);
        chatMessage.setSender(displayName);  // Set the correct sender name

        if (chatMessage.getRoom() != null && !chatMessage.getRoom().trim().isEmpty()) {
            messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoom(), chatMessage);
        } else {
            messagingTemplate.convertAndSend("/topic/global", chatMessage);
        }
    }

    @MessageMapping("/chat.removeUser")
    public void removeUser(@Payload ChatMessage chatMessage){
        chatMessage.setType(MessageType.LEAVE);
        if(chatMessage.getRoom() != null){
            messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoom(), chatMessage);
        } else {
            messagingTemplate.convertAndSend("/topic/global", chatMessage);
        }
    }
}

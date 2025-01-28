package com.example.blog.service;

import com.example.blog.model.Message;
import com.example.blog.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository){
        this.messageRepository = messageRepository;
    }

    public Message saveMessage(Message message){
        message.setTimestamp(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getMessagesByRoom(String room){
        return messageRepository.findByRoomOrderByTimestampAsc(room);
    }

    public List<Message> getPrivateMessages(String recipient){
        return messageRepository.findByRecipientOrderByTimestampAsc(recipient);
    }

}

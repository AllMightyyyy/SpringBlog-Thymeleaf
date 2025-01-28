package com.example.blog.repository;

import com.example.blog.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomOrderByTimestampAsc(String room);
    List<Message> findByRecipientOrderByTimestampAsc(String recipient);
}

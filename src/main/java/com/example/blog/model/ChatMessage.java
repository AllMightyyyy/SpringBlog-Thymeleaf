package com.example.blog.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatMessage {
    private MessageType type;
    private String content;
    private String sender;
    private String recipient; // For private messages
    private String room; // For room-based messages
}

package com.example.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.example.blog.model.User;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendCommentNotification(User toUser, String postTitle, String postUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toUser.getEmail());
        message.setSubject("New Comment on Your Subscribed Post: " + postTitle);
        message.setText("Hello " + toUser.getName() + ",\n\n" +
                "A new comment has been added to the post \"" + postTitle + "\".\n" +
                "You can view the comment here: " + postUrl + "\n\n" +
                "Best regards,\nBlogio Team");
        mailSender.send(message);
    }
}

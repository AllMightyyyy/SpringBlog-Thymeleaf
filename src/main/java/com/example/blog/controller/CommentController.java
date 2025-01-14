package com.example.blog.controller;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.model.Subscription;
import com.example.blog.model.User;
import com.example.blog.service.CommentService;
import com.example.blog.service.PostService;
import com.example.blog.service.SubscriptionService;
import com.example.blog.service.EmailService;
import com.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;

    @Autowired
    public CommentController(CommentService commentService,
                             PostService postService,
                             UserRepository userRepository,
                             SubscriptionService subscriptionService,
                             EmailService emailService) {
        this.commentService = commentService;
        this.postService = postService;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.emailService = emailService;
    }

    @PostMapping("/posts/{id}/comments")
    @Transactional
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        // 1) Find current user by email
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) Fetch the post
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // 3) Create the comment
        Comment comment = Comment.builder()
                .content(content)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        // 4) Associate comment with post using helper method
        post.addComment(comment);

        // 5) Save the post (cascades to comment)
        postService.save(post);

        // 6) Notify subscribers (except the commenter)
        List<Subscription> subscriptions = subscriptionService.findByPost(post);
        for (Subscription sub : subscriptions) {
            if (!sub.getUser().getEmail().equals(user.getEmail())) {
                // Send email notification
                String postUrl = "http://localhost:8080/posts/" + post.getId(); // Adjust as needed
                emailService.sendCommentNotification(
                        sub.getUser(),
                        post.getTitle(),
                        postUrl
                );
            }
        }

        redirectAttributes.addFlashAttribute("success", "Comment added successfully!");
        return "redirect:/posts/" + id;
    }
}

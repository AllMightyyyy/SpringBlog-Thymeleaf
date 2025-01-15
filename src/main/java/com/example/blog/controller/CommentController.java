package com.example.blog.controller;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.model.Subscription;
import com.example.blog.model.User;
import com.example.blog.service.CommentService;
import com.example.blog.service.PostService;
import com.example.blog.service.SubscriptionService;
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

    @Autowired
    public CommentController(CommentService commentService,
                             PostService postService,
                             UserRepository userRepository,
                             SubscriptionService subscriptionService) {
        this.commentService = commentService;
        this.postService = postService;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/posts/{id}/comments")
    @Transactional
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        Comment comment = Comment.builder()
                .content(content)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        post.addComment(comment);

        postService.save(post);

        redirectAttributes.addFlashAttribute("success", "Comment added successfully!");
        return "redirect:/posts/" + id;
    }
}

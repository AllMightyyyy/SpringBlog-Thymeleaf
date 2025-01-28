package com.example.blog.controller;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.model.User;
import com.example.blog.service.CommentService;
import com.example.blog.service.ContentFilterService;
import com.example.blog.service.EmailService;
import com.example.blog.service.PostService;
import com.example.blog.repository.UserRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ContentFilterService contentFilterService;

    @Autowired
    public CommentController(CommentService commentService,
                             PostService postService,
                             UserRepository userRepository) {
        this.commentService = commentService;
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts/{id}/comments")
    @Transactional
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             @RequestParam(required = false) Long parentId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        String sanitizedContent = Jsoup.clean(content, Safelist.basicWithImages());


        Comment comment = Comment.builder()
                .content(sanitizedContent)
                .createdAt(LocalDateTime.now())
                .user(user)
                .post(post)
                .build();

        comment.setContent(sanitizedContent);

        if (!contentFilterService.isContentSafe(comment.getContent())) {
            comment.setReported(true);
            comment.setReportCount(comment.getReportCount() + 1);
            emailService.sendNotificationToAdmin(
                    "Potentially Harmful Comment Detected",
                    "A comment by " + user.getName() + " on post ID " + post.getId() + " contains potentially harmful content and has been flagged for review."
            );
            redirectAttributes.addFlashAttribute("error", "Your comment contains inappropriate content and cannot be posted.");
            return "redirect:/posts/" + id;
        }

        emailService.sendNotificationToAdmin(
                "New Comment Added",
                "A new comment has been added to post ID " + post.getId() + " by " + user.getName() + " (" + user.getEmail() + ")."
        );

        if (parentId != null) {
            Comment parentComment = commentService.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            parentComment.addReply(comment);
            commentService.save(parentComment);
        } else {
            post.addComment(comment);
            postService.save(post);
        }

        redirectAttributes.addFlashAttribute("success", "Comment added successfully!");
        return "redirect:/posts/" + id;
    }
}

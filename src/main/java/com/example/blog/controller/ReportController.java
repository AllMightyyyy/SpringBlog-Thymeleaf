package com.example.blog.controller;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.service.CommentService;
import com.example.blog.service.EmailService;
import com.example.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EmailService emailService;

    /**
     * Report a post
     */
    @PostMapping("/post/{id}")
    public String reportPost(@PathVariable Long id, @RequestParam String reason, RedirectAttributes redirectAttributes) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));
        post.setReportCount(post.getReportCount() + 1);
        if (post.getReportCount() >= 5) { // Threshold for auto-reporting
            post.setReported(true);
            emailService.sendNotificationToAdmin(
                    "Post Automatically Reported",
                    "Post ID " + id + " has been reported " + post.getReportCount() + " times and has been flagged for review."
            );
        }
        postService.save(post);
        redirectAttributes.addFlashAttribute("success", "Post reported successfully!");
        return "redirect:/posts/" + id;
    }

    /**
     * Report a comment
     */
    @PostMapping("/comment/{id}")
    public String reportComment(@PathVariable Long id, @RequestParam String reason, RedirectAttributes redirectAttributes) {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment Id:" + id));
        comment.setReportCount(comment.getReportCount() + 1);
        if (comment.getReportCount() >= 3) { // Threshold for auto-reporting
            comment.setReported(true);
            emailService.sendNotificationToAdmin(
                    "Comment Automatically Reported",
                    "Comment ID " + id + " has been reported " + comment.getReportCount() + " times and has been flagged for review."
            );
        }
        commentService.save(comment);
        redirectAttributes.addFlashAttribute("success", "Comment reported successfully!");
        return "redirect:/posts/" + comment.getPost().getId();
    }
}

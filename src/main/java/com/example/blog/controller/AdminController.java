package com.example.blog.controller;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.service.CommentService;
import com.example.blog.service.EmailService;
import com.example.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PostService postService;
    private final CommentService commentService;
    private final EmailService emailService;

    @Autowired
    public AdminController(PostService postService, CommentService commentService, EmailService emailService) {
        this.postService = postService;
        this.commentService = commentService;
        this.emailService = emailService;
    }

    /**
     * View all reported posts
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reported/posts")
    public String viewReportedPosts(Model model) {
        List<Post> reportedPosts = postService.findReportedPosts();
        model.addAttribute("reportedPosts", reportedPosts);
        return "admin/reported_posts";
    }

    /**
     * View all reported comments
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reported/comments")
    public String viewReportedComments(Model model) {
        List<Comment> reportedComments = commentService.findReportedComments();
        model.addAttribute("reportedComments", reportedComments);
        return "admin/reported_comments";
    }

    /**
     * Delete any post
     */
    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        String postOwnerEmail = post.getAuthor().getEmail();

        postService.deleteById(id);
        emailService.sendNotificationToUser(
                postOwnerEmail,
                "Your post has been deleted",
                "Your post titled \"" + post.getTitle() + "\" has been deleted by the administrator."
        );

        redirectAttributes.addFlashAttribute("success", "Post deleted successfully!");
        return "redirect:/admin/reported/posts";
    }

    /**
     * Delete any comment
     */
    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment Id:" + id));

        commentService.deleteById(id);
        emailService.sendNotificationToAdmin(
                "Comment Deleted by Admin",
                "A comment by \"" + comment.getUser().getName() + "\" has been deleted by the administrator."
        );

        redirectAttributes.addFlashAttribute("success", "Comment deleted successfully!");
        return "redirect:/admin/reported/comments";
    }

}

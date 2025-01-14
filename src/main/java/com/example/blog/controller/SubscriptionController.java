package com.example.blog.controller;

import com.example.blog.model.Post;
import com.example.blog.model.User;
import com.example.blog.service.PostService;
import com.example.blog.service.SubscriptionService;
import com.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final PostService postService;
    private final UserRepository userRepository;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService,
                                  PostService postService,
                                  UserRepository userRepository) {
        this.subscriptionService = subscriptionService;
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @PostMapping("/subscribe/{postId}")
    public String subscribe(@PathVariable Long postId, Authentication auth, RedirectAttributes redirectAttributes) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Post post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id: " + postId));

        subscriptionService.subscribe(user, post);
        redirectAttributes.addFlashAttribute("success", "Subscribed to post successfully!");
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/unsubscribe/{postId}")
    public String unsubscribe(@PathVariable Long postId, Authentication auth, RedirectAttributes redirectAttributes) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Post post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id: " + postId));

        subscriptionService.unsubscribe(user, post);
        redirectAttributes.addFlashAttribute("success", "Unsubscribed from post successfully!");
        return "redirect:/posts/" + postId;
    }
}

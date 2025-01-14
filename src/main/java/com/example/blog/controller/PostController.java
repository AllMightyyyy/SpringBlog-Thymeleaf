package com.example.blog.controller;

import com.example.blog.model.Post;
import com.example.blog.model.Reaction;
import com.example.blog.model.Tag;
import com.example.blog.model.User;
import com.example.blog.service.PostService;
import com.example.blog.service.ReactionService;
import com.example.blog.service.SubscriptionService;
import com.example.blog.service.TagService;
import com.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class PostController {

    private final PostService postService;
    private final ReactionService reactionService;
    private final TagService tagService;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired
    public PostController(PostService postService,
                          ReactionService reactionService,
                          TagService tagService,
                          UserRepository userRepository,
                          SubscriptionService subscriptionService) {
        this.postService = postService;
        this.reactionService = reactionService;
        this.tagService = tagService;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);
        return "home";
    }

    @GetMapping("/posts/create")
    public String showCreatePostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create_post";
    }

    @PostMapping("/posts/create")
    @Transactional
    public String createPost(@ModelAttribute Post post,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam(value = "tags", required = false) String tagsInput,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        // 1) Retrieve logged-in user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) Set post fields
        post.setAuthor(user);
        post.setCreatedAt(LocalDateTime.now());

        // 3) Handle image upload
        if (!imageFile.isEmpty()) {
            try {
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs();
                }

                String originalFilename = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                }
                String uniqueFilename = UUID.randomUUID().toString() + extension;

                File destinationFile = Paths.get(uploadDir, uniqueFilename).toFile();
                imageFile.transferTo(destinationFile);

                post.setImagePath("/uploads/" + uniqueFilename);
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Failed to upload image.");
                return "redirect:/posts/create";
            }
        }

        // 4) Parse the tags (if any)
        if (tagsInput != null && !tagsInput.trim().isEmpty()) {
            String[] tagNames = tagsInput.split(",");
            Set<Tag> tagSet = new HashSet<>();
            for (String tagName : tagNames) {
                tagName = tagName.trim();
                if (!tagName.isEmpty()) {
                    Tag tag = tagService.findOrCreateTag(tagName);
                    tagSet.add(tag);
                    post.addTag(tag);
                }
            }
            post.setTags(tagSet);
        }

        // 5) Save the post
        postService.save(post);
        redirectAttributes.addFlashAttribute("success", "Post created successfully!");
        return "redirect:/";
    }

    @GetMapping("/posts/{id}")
    @Transactional
    public String viewPost(@PathVariable Long id,
                           Model model,
                           @RequestParam(value = "error", required = false) String error,
                           Authentication authentication) {

        // 1) Retrieve the Post
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // 2) Count like/dislike
        long likeCount = reactionService.countByPostAndType(post, "LIKE");
        long dislikeCount = reactionService.countByPostAndType(post, "DISLIKE");

        // 3) Pass to the view
        model.addAttribute("post", post);
        model.addAttribute("comments", post.getComments());
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("dislikeCount", dislikeCount);
        model.addAttribute("error", error);

        // 4) Check subscription status for the logged-in user
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                boolean isSubscribed = subscriptionService.isSubscribed(user, post);
                model.addAttribute("isSubscribed", isSubscribed);
            }
        }
        return "view_post";
    }

    @PostMapping("/posts/{id}/react")
    @Transactional
    public String reactToPost(@PathVariable Long id,
                              @RequestParam String type,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        // 1) Ensure user is found
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) Retrieve the post
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // 3) Check existing reaction
        Optional<Reaction> existingReaction = reactionService.findByUserAndPost(user, post);
        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            reaction.setType(type);
            reactionService.save(reaction);
        } else {
            Reaction reaction = Reaction.builder()
                    .user(user)
                    .post(post)
                    .type(type)
                    .build();
            reactionService.save(reaction);
        }

        redirectAttributes.addFlashAttribute("success", "Your reaction has been recorded.");
        return "redirect:/posts/" + id;
    }

    @GetMapping("/posts/{id}/edit")
    @Transactional
    public String showEditPostForm(@PathVariable Long id,
                                   Model model,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {

        // 1) Retrieve the post
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // 2) Check ownership
        String email = authentication.getName();
        if (!post.getAuthor().getEmail().equals(email)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this post.");
            return "redirect:/posts/" + id;
        }

        // 3) Pass to the model
        model.addAttribute("post", post);

        // Prepare tags as comma-separated string
        String tags = String.join(", ",
                post.getTags().stream().map(Tag::getName).toArray(String[]::new));
        model.addAttribute("tagsInput", tags);

        return "edit_post";
    }

    @PostMapping("/posts/{id}/edit")
    @Transactional
    public String editPost(@PathVariable Long id,
                           @ModelAttribute Post updatedPost,
                           @RequestParam("imageFile") MultipartFile imageFile,
                           @RequestParam(value = "tags", required = false) String tagsInput,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

        // 1) Retrieve existing post
        Post existingPost = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // 2) Check ownership
        String email = authentication.getName();
        if (!existingPost.getAuthor().getEmail().equals(email)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this post.");
            return "redirect:/posts/" + id;
        }

        // 3) Update fields
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());

        // 4) Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs();
                }

                String originalFilename = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                }
                String uniqueFilename = UUID.randomUUID().toString() + extension;

                File destinationFile = Paths.get(uploadDir, uniqueFilename).toFile();
                imageFile.transferTo(destinationFile);

                // If there was an old image, try to delete it
                if (existingPost.getImagePath() != null) {
                    String oldImageName = existingPost.getImagePath().replace("/uploads/", "");
                    File oldImage = new File(uploadDir, oldImageName);
                    if (oldImage.exists()) {
                        oldImage.delete();
                    }
                }

                // Set new image path
                existingPost.setImagePath("/uploads/" + uniqueFilename);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Failed to upload new image.");
                return "redirect:/posts/" + id + "/edit";
            }
        }

        // 5) Handle Tags (if any)
        if (tagsInput != null && !tagsInput.trim().isEmpty()) {
            String[] tagNames = tagsInput.split(",");
            Set<Tag> newTagSet = new HashSet<>();
            for (String tagName : tagNames) {
                tagName = tagName.trim();
                if (!tagName.isEmpty()) {
                    Tag tag = tagService.findOrCreateTag(tagName);
                    newTagSet.add(tag);
                }
            }

            // Remove existing tags not in newTagSet
            Set<Tag> tagsToRemove = new HashSet<>(existingPost.getTags());
            tagsToRemove.removeAll(newTagSet);
            for (Tag tag : tagsToRemove) {
                existingPost.removeTag(tag);
            }

            // Add new tags
            for (Tag tag : newTagSet) {
                existingPost.addTag(tag);
            }
        } else {
            // If user leaves tag field blank, clear all tags
            for (Tag tag : new HashSet<>(existingPost.getTags())) {
                existingPost.removeTag(tag);
            }
        }

        // 6) Save
        try {
            postService.save(existingPost);
            redirectAttributes.addFlashAttribute("success", "Post updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating the post.");
        }

        return "redirect:/posts/" + id;
    }
}

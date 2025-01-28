package com.example.blog.controller;

import com.example.blog.model.*;
import com.example.blog.service.*;
import com.example.blog.repository.UserRepository;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
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
    private final CommentService commentService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ContentFilterService contentFilterService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    @Autowired
    public PostController(PostService postService,
                          ReactionService reactionService,
                          TagService tagService,
                          UserRepository userRepository,
                          SubscriptionService subscriptionService,
                          CommentService commentService) {
        this.postService = postService;
        this.reactionService = reactionService;
        this.tagService = tagService;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.commentService = commentService;

        this.markdownParser = Parser.builder().build();
        this.htmlRenderer = HtmlRenderer.builder().build();
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);
        return "home";
    }

    @GetMapping("/my-posts")
    public String myPosts(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Post> posts = postService.findByAuthor(user);
        model.addAttribute("posts", posts);
        return "my_posts";
    }

    @PostMapping("/posts/{id}/delete")
    @Transactional
    public String deletePost(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));
        String email = authentication.getName();
        if (!post.getAuthor().getEmail().equals(email)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this post.");
            return "redirect:/posts/" + id;
        }

        if (post.getImagePath() != null) {
            File imageFile = new File(uploadDir, post.getImagePath().replace("/uploads/", ""));
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }

        for (Subscription sub : new HashSet<>(post.getSubscriptions())) {
            post.removeSubscription(sub);
        }

        postService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Post deleted successfully!");
        return "redirect:/my-posts";
    }

    @GetMapping("/posts/create")
    public String showCreatePostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create_post";
    }

    @PostMapping("/posts/create")
    @Transactional
    public String createPost(@ModelAttribute Post post,
                             @RequestParam(value = "tagsInput", required = false) String tagsInput,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        post.setAuthor(user);
        post.setCreatedAt(LocalDateTime.now());

        String sanitizedContent = Jsoup.clean(post.getContent(), Safelist.basicWithImages());
        post.setContent(sanitizedContent);

        // Auto-flagging check
        if (!contentFilterService.isContentSafe(post.getContent())) {
            post.setReported(true);
            post.setReportCount(post.getReportCount() + 1);
            emailService.sendNotificationToAdmin(
                    "Potentially Harmful Post Detected",
                    "A post titled \"" + post.getTitle() + "\" contains potentially harmful content and has been flagged for review."
            );
            redirectAttributes.addFlashAttribute("error", "Your post contains inappropriate content and cannot be published.");
            return "redirect:/posts/create";
        }

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

        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        long likeCount = reactionService.countByPostAndType(post, "LIKE");
        long dislikeCount = reactionService.countByPostAndType(post, "DISLIKE");

        String renderedContent = htmlRenderer.render(markdownParser.parse(post.getContent()));

        List<Comment> topLevelComments = commentService.findTopLevelCommentsByPostId(id);

        model.addAttribute("post", post);
        model.addAttribute("comments", topLevelComments);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("dislikeCount", dislikeCount);
        model.addAttribute("error", error);
        model.addAttribute("renderedContent", renderedContent);

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

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

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

        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        String email = authentication.getName();
        if (!post.getAuthor().getEmail().equals(email)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this post.");
            return "redirect:/posts/" + id;
        }

        model.addAttribute("post", post);

        String tags = String.join(", ",
                post.getTags().stream().map(Tag::getName).toArray(String[]::new));
        model.addAttribute("tagsInput", tags);

        return "edit_post";
    }

    @PostMapping("/posts/{id}/edit")
    @Transactional
    public String editPost(@PathVariable Long id,
                           @ModelAttribute Post updatedPost,
                           @RequestParam(value = "tagsInput", required = false) String tagsInput,
                           @RequestParam("imageFile") MultipartFile imageFile,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

        Post existingPost = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        String email = authentication.getName();
        if (!existingPost.getAuthor().getEmail().equals(email)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this post.");
            return "redirect:/posts/" + id;
        }

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());

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

                if (existingPost.getImagePath() != null) {
                    String oldImageName = existingPost.getImagePath().replace("/uploads/", "");
                    File oldImage = new File(uploadDir, oldImageName);
                    if (oldImage.exists()) {
                        oldImage.delete();
                    }
                }

                existingPost.setImagePath("/uploads/" + uniqueFilename);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Failed to upload new image.");
                return "redirect:/posts/" + id + "/edit";
            }
        }

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

            Set<Tag> tagsToRemove = new HashSet<>(existingPost.getTags());
            tagsToRemove.removeAll(newTagSet);
            for (Tag tag : tagsToRemove) {
                existingPost.removeTag(tag);
            }

            for (Tag tag : newTagSet) {
                existingPost.addTag(tag);
            }
        } else {
            for (Tag tag : new HashSet<>(existingPost.getTags())) {
                existingPost.removeTag(tag);
            }
        }

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

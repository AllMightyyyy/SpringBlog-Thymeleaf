package com.example.blog.controller;

import com.example.blog.model.Post;
import com.example.blog.service.PostService;
import com.example.blog.service.TagService;
import com.example.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final PostService postService;
    private final TagService tagService;
    private final UserService userService;

    @Autowired
    public SearchController(PostService postService, TagService tagService, UserService userService) {
        this.postService = postService;
        this.tagService = tagService;
        this.userService = userService;
    }

    /**
     * Display the search form.
     */
    @GetMapping
    public String showSearchForm(Model model) {
        model.addAttribute("tags", tagService.findAll());
        return "search";
    }

    /**
     * Handle search requests and display results with pagination.
     */
    @GetMapping("/results")
    public String searchPosts(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "authorEmail", required = false) String authorEmail,
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "createdAfter", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(value = "createdBefore", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postService.searchPosts(title, tag, authorEmail, authorName, createdAfter, createdBefore, pageable);

        model.addAttribute("title", title);
        model.addAttribute("tag", tag);
        model.addAttribute("authorEmail", authorEmail);
        model.addAttribute("authorName", authorName);
        model.addAttribute("createdAfter", createdAfter);
        model.addAttribute("createdBefore", createdBefore);

        model.addAttribute("postPage", postPage);
        return "search_results";
    }
}

package com.example.blog.controller;

import com.example.blog.model.Tag;
import com.example.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public String showAllTags(Model model) {
        model.addAttribute("tags", tagService.findAll());
        return "tags_list";
    }

    @GetMapping("/tags/{id}")
    @Transactional
    public String showPostsByTag(@PathVariable Long id, Model model) {
        Tag tag = tagService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tag ID: " + id));
        model.addAttribute("tag", tag);
        model.addAttribute("posts", tag.getPosts());
        return "tag_posts";
    }
}

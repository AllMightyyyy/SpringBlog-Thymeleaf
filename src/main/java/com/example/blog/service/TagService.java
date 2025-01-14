package com.example.blog.service;

import com.example.blog.model.Tag;
import com.example.blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    // Create or find a tag by name
    public Tag findOrCreateTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
    }

    // Save a Tag entity
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }

    // Find all tags
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    // Find tag by ID
    public Optional<Tag> findById(Long id) {
        return tagRepository.findById(id);
    }

    // Find tag by name
    public Optional<Tag> findByName(String name){
        return tagRepository.findByName(name);
    }
}

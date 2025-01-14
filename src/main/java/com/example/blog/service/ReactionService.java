package com.example.blog.service;

import com.example.blog.model.Reaction;
import com.example.blog.model.User;
import com.example.blog.model.Post;
import com.example.blog.repository.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;

    @Autowired
    public ReactionService(ReactionRepository reactionRepository){
        this.reactionRepository = reactionRepository;
    }

    public Optional<Reaction> findByUserAndPost(User user, Post post){
        return reactionRepository.findByUserAndPost(user, post);
    }

    public Reaction save(Reaction reaction){
        return reactionRepository.save(reaction);
    }

    // Count how many likes or dislikes a post has
    public long countByPostAndType(Post post, String type) {
        return reactionRepository.countByPostAndType(post, type);
    }
}

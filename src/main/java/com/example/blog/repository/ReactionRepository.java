package com.example.blog.repository;

import com.example.blog.model.Reaction;
import com.example.blog.model.User;
import com.example.blog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByUserAndPost(User user, Post post);

    // Count how many likes or dislikes a post has
    long countByPostAndType(Post post, String type);
}

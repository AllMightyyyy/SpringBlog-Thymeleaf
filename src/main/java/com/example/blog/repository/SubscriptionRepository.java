package com.example.blog.repository;

import com.example.blog.model.Subscription;
import com.example.blog.model.User;
import com.example.blog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserAndPost(User user, Post post);
    List<Subscription> findByPost(Post post);
    void deleteByPost(Post post);
}

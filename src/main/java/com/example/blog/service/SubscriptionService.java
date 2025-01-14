package com.example.blog.service;

import com.example.blog.model.Subscription;
import com.example.blog.model.User;
import com.example.blog.model.Post;
import com.example.blog.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription subscribe(User user, Post post) {
        Optional<Subscription> existing = subscriptionRepository.findByUserAndPost(user, post);
        if (existing.isPresent()) {
            return existing.get();
        }
        Subscription subscription = Subscription.builder()
                .user(user)
                .post(post)
                .build();
        return subscriptionRepository.save(subscription);
    }

    public void unsubscribe(User user, Post post) {
        subscriptionRepository.findByUserAndPost(user, post)
                .ifPresent(subscriptionRepository::delete);
    }

    public List<Subscription> findByPost(Post post) {
        return subscriptionRepository.findByPost(post);
    }

    public boolean isSubscribed(User user, Post post) {
        return subscriptionRepository.findByUserAndPost(user, post).isPresent();
    }
}

package com.example.blog.service;

import com.example.blog.model.Post;
import com.example.blog.model.User;
import com.example.blog.repository.PostRepository;
import com.example.blog.specification.PostSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository){
        this.postRepository = postRepository;
    }

    public List<Post> findAll(){
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Optional<Post> findById(Long id){
        return postRepository.findById(id);
    }

    public Post save(Post post){
        return postRepository.save(post);
    }

    public void deleteById(Long id){
        postRepository.deleteById(id);
    }

    public List<Post> findByAuthor(User author){
        return postRepository.findByAuthor(author, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Page<Post> searchPosts(String title, String tag, String authorEmail, String authorName,
                                  LocalDateTime createdAfter, LocalDateTime createdBefore,
                                  Pageable pageable) {
        Specification<Post> spec = Specification.where(PostSpecification.hasTitleLike(title))
                .and(PostSpecification.hasTag(tag))
                .and(PostSpecification.hasAuthorEmail(authorEmail))
                .and(PostSpecification.hasAuthorName(authorName))
                .and(PostSpecification.createdAfter(createdAfter))
                .and(PostSpecification.createdBefore(createdBefore));
        return postRepository.findAll(spec, pageable);
    }

    public List<Post> findReportedPosts() {
        return postRepository.findByReportedTrue();
    }
}

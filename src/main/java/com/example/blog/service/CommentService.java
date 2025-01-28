package com.example.blog.service;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository){
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment save(Comment comment){
        return commentRepository.save(comment);
    }

    public Optional<Comment> findById(Long id){
        return commentRepository.findById(id);
    }

    public void deleteById(Long id){
        commentRepository.deleteById(id);
    }

    /**
     * Fetches all top-level comments for a given post, ordered by creation time.
     *
     * @param postId The ID of the post.
     * @return A list of top-level comments.
     */
    public List<Comment> findTopLevelCommentsByPostId(Long postId){
        return commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);
    }

    public List<Comment> findReportedComments() {
        return commentRepository.findByReportedTrue();
    }
}

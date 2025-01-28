package com.example.blog.repository;

import com.example.blog.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Finds all comments for a given post where the parent is null (top-level comments).
     *
     * @param postId The ID of the post.
     * @return A list of top-level comments ordered by creation date ascending.
     */
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);
    List<Comment> findByReportedTrue();
}

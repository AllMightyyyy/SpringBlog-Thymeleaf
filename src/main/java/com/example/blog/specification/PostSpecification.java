package com.example.blog.specification;

import com.example.blog.model.Post;
import com.example.blog.model.Tag;
import com.example.blog.model.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;

public class PostSpecification {

    public static Specification<Post> hasTitleLike(String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Post> hasTag(String tagName) {
        return (root, query, criteriaBuilder) -> {
            if (tagName == null || tagName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Post, Tag> tags = root.join("tags", JoinType.INNER);
            return criteriaBuilder.equal(criteriaBuilder.lower(tags.get("name")), tagName.toLowerCase());
        };
    }

    public static Specification<Post> hasAuthorEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Post, User> author = root.join("author", JoinType.INNER);
            return criteriaBuilder.equal(criteriaBuilder.lower(author.get("email")), email.toLowerCase());
        };
    }

    public static Specification<Post> hasAuthorName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Post, User> author = root.join("author", JoinType.INNER);
            return criteriaBuilder.like(criteriaBuilder.lower(author.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Post> createdAfter(LocalDateTime dateTime) {
        return (root, query, criteriaBuilder) -> {
            if (dateTime == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Post> createdBefore(LocalDateTime dateTime) {
        return (root, query, criteriaBuilder) -> {
            if (dateTime == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), dateTime);
        };
    }
}

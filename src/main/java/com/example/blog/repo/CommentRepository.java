package com.example.blog.repo;

import com.example.blog.models.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    Iterable<Comment> findByPostId(Long post_id);

    @Transactional
    void deleteByPostId(Long post_id);

    Iterable<Comment> findByAuthorId(Long author_id);

    @Transactional
    void deleteByAuthorId(Long author_id);
}

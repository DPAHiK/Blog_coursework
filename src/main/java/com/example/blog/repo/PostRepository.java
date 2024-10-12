package com.example.blog.repo;

import com.example.blog.models.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Long> {
    Iterable<Post> findByOwnerId(Long owner_id);

    @Transactional
    void deleteByOwnerId(Long owner_id);
}

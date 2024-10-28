package com.example.blog.repo;

import com.example.blog.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.blog.models.UserInfo;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Iterable<Post> findByUserId(Long user_id);
}
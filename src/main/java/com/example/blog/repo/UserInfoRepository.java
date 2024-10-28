package com.example.blog.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import com.example.blog.models.UserInfo;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByUserId(Long user_id);
}
package com.example.blog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.blog.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String username);
}
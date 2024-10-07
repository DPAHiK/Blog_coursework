package com.example.blog.services;


import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.blog.models.Post;
import com.example.blog.models.User;
import com.example.blog.repo.UserRepository;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class PostService {
    private List<Post> posts;
    private UserRepository repository;
    private PasswordEncoder passwordEncoder;

    public PostService(List<Post> posts, UserRepository repository) {
        this.posts = posts;
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public List<Post> allPosts() {
        return posts;
    }

    public Post postByID(int id) {
        return posts.stream()
                .filter(post -> post.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
    }
}

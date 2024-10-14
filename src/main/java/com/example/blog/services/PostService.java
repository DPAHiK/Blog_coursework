package com.example.blog.services;


import com.example.blog.repo.PostRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.blog.models.Post;
import com.example.blog.models.User;
import com.example.blog.repo.UserRepository;

import java.util.Optional;

@Service
public class PostService {
    private Iterable<Post> posts;
    private UserRepository userRepository;
    private PostRepository postRepository;
    private PasswordEncoder passwordEncoder;

    public PostService(UserRepository userRepository, PostRepository postRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
        posts = postRepository.findAll();
    }

    public Iterable<Post> allPosts() {
        return posts;
    }

    public Optional<Post> postByID(long id) {
        Optional<Post> post = Optional.empty();
        for (Post post1 : posts) {
            if (post1.getId() == id) {
                post = Optional.of(post1);
            }
        }
        return post;
    }

    public Optional<User> userByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> userById(Long id) {
        return userRepository.findById(id);
    }

    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void addPost(Post post) {
        postRepository.save(post);
        this.posts = postRepository.findAll(); // мб получше что-то?
    }

    public void deletePost(Post post) {
        postRepository.delete(post);
        this.posts = postRepository.findAll();
    }
}

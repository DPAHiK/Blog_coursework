package com.example.blog.services;


import com.example.blog.models.UserInfo;
import com.example.blog.repo.PostRepository;
import com.example.blog.repo.UserInfoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.blog.models.Post;
import com.example.blog.models.User;
import com.example.blog.repo.UserRepository;

import java.util.Optional;

@Service
public class PostService { //Он вообще нужен?
    private Iterable<Post> posts;
    private UserRepository userRepository;
    private UserInfoRepository userInfoRepository;
    private PostRepository postRepository;
    private PasswordEncoder passwordEncoder;

    public PostService(UserRepository userRepository, UserInfoRepository userInfoRepository, PostRepository postRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userInfoRepository = userInfoRepository;
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

    public Optional<UserInfo> infoByUserID(Long id){return userInfoRepository.findByUserId(id);}

    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void addUserInfo(UserInfo userInfo){
        userInfoRepository.save(userInfo);
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

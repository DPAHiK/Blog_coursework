package com.example.blog.services;


import com.example.blog.repo.PostRepository;
import org.springframework.stereotype.Service;
import com.example.blog.models.Post;

import java.util.List;
import java.util.Optional;


@Service
public class PostService { //Он вообще нужен?
    private final List<Post> posts;
    private final PostRepository postRepository;

    public PostService( PostRepository postRepository) {
        this.postRepository = postRepository;
        posts = (List<Post>) postRepository.findAll();
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


    public void addPost(Post post) {
        postRepository.save(post);
        posts.add(post);
    }

    public void deletePost(Post post) {
        postRepository.delete(post);
        posts.remove(post);
    }
}

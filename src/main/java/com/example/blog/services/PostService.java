package com.example.blog.services;


import com.example.blog.repo.PostRepository;
import org.springframework.stereotype.Service;
import com.example.blog.models.Post;

import java.util.ArrayList;
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

    public List<Post> allPosts() {
        return posts;
    }

    public List<Post> somePosts(int from, int count){
        List<Post> partPosts = new ArrayList<>();
        int size = Math.min(posts.size(), from + count);
        for (int i = from; i < size; i++) partPosts.add(posts.get(i));

        return partPosts;
    }

    public List<Post> somePosts(int from, int count, String filterName){
        filterName = filterName.strip();

        List<Post> partPosts = new ArrayList<>();
        int i = 0;
        int j = 0;
        for (; j < posts.size() && i < from; j++)
            if(posts.get(j).getTitle().contains(filterName) ||
                    posts.get(j).getAnons().contains(filterName)) i++;


        for (; j < posts.size() && partPosts.size() < count; j++){
            if(posts.get(j).getTitle().contains(filterName) ||
                    posts.get(j).getAnons().contains(filterName)) partPosts.add(posts.get(j));
        }

        return partPosts;
    }

    public int postsCount(){
        return posts.size();
    }

    public int postsCount(String filter){
        List<Post> partPosts = new ArrayList<>();
        for (int i = 0; i < posts.size(); i++)
            if(posts.get(i).getTitle().contains(filter) ||
                    posts.get(i).getAnons().contains(filter)) partPosts.add(posts.get(i));
        return partPosts.size();
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

    public List<Post> postByOwnerId(long ownerId){
        List<Post> foundPosts = new ArrayList<Post>();
        for (Post post1 : posts) {
            if (post1.getOwner().getId() == ownerId) {
                foundPosts.add(post1);
            }
        }

        return foundPosts;
    }

    public List<Post> postByOwnerId(long ownerId, int count){
        List<Post> foundPosts = new ArrayList<Post>();
        for (int i = 0; i < this.posts.size() && foundPosts.size() < count; i++) {
            if (this.posts.get(i).getOwner().getId() == ownerId) {
                foundPosts.add(posts.get(i));
            }
        }

        return foundPosts;
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

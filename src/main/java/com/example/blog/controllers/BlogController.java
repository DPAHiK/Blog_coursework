package com.example.blog.controllers;

import com.example.blog.models.Comment;
import com.example.blog.models.Post;
import com.example.blog.models.User;
import com.example.blog.services.PostService;
import com.example.blog.services.CommentService;
import com.example.blog.services.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@Controller
public class BlogController {

    @Autowired
    private final PostService postService;
    @Autowired
    private final UserInfoService userService;
    @Autowired
    private final CommentService commentService;


    public BlogController(PostService postService, UserInfoService userService, CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return !auth.getName().equals("anonymousUser");


    }

    @GetMapping("/")
    public String blogMain(Model model){
        model.addAttribute("auth", isAuthenticated());

        Optional <User> curUser = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                () -> model.addAttribute("curUser", null));

        Iterable<Post> posts = postService.allPosts();
        model.addAttribute("posts", posts);
        return "blog-main";
    }

    @GetMapping("/blog/add")
    public String blogAdd(Model model){
        model.addAttribute("auth", isAuthenticated());

        Optional <User> curUser = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                () -> model.addAttribute("curUser", null));

        return "blog-add";
    }

    @PostMapping("/blog/add")
    public String blogPostAdd(@RequestParam String title,
                              @RequestParam String anons,
                              @RequestParam String full_text,
                              Model model ){
        model.addAttribute("auth", isAuthenticated());

        Optional <User> curUser = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                () -> model.addAttribute("curUser", null));

        if(title.isEmpty() || anons.isEmpty() || full_text.isEmpty()) {
            model.addAttribute("errors", "Все поля должны быть заполнены");
            return "blog-add";
        }
        Post post = new Post(title, anons, full_text);
        Optional<User> user = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user.isEmpty()) {
            model.addAttribute("errors", "Ошибка при создании поста: пользователь не найден");
            System.out.println("Error when trying to add post: user not found");
            return "blog-add";
        }

        post.setOwner(user.get());
        postService.addPost(post);
        return "redirect:/";
    }

    @GetMapping("/blog/{id}")
    public String blogDetails(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("auth", isAuthenticated());
        Optional<Post> post = postService.postByID(id);
        if (post.isEmpty()) return "redirect:/";

        model.addAttribute("post", post.get());

        Optional <User> curUser = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                                    () -> model.addAttribute("curUser", null));

        Optional <User> owner = userService.userById(post.get().getOwner().getId());
        owner.ifPresentOrElse((user) -> model.addAttribute("owner", user),
                () -> model.addAttribute("owner", null));

        Iterable<Comment> comments = commentService.commentsByPost(post.get().getId());
        model.addAttribute("comments", comments);
        return "blog-details";
    }



    @GetMapping("/blog/{id}/edit")
    public String blogEdit(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("auth", isAuthenticated());

        Optional <User> curUser = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                () -> model.addAttribute("curUser", null));

        Optional<Post> post = postService.postByID(id);
        if (post.isEmpty()) return "redirect:/";

        model.addAttribute("post", post.get());
        return "blog-edit";
    }

    @PostMapping("/blog/{id}/edit")
    public String blogPostUpdate(@RequestParam String title,
                                 @RequestParam String anons,
                                 @RequestParam String full_text,
                                 @PathVariable(value = "id") long id,
                                 Model model ){
        Post post = postService.postByID(id).orElseThrow();
        post.setTitle(title);
        post.setAnons(anons);
        post.setFull_text(full_text);
        postService.addPost(post);

        return "redirect:/";
    }

    @PostMapping("/blog/{id}/remove")
    public String blogPostRemove(@PathVariable(value = "id") long id,
                                 Model model ){
        Post post = postService.postByID(id).orElseThrow();
        postService.deletePost(post);

        return "redirect:/";
    }

    @PostMapping("/blog/{id}/addComment")
    public String addComment(Model model,
                             @PathVariable(value = "id") long postId,
                             @RequestParam String full_text){
        Optional<Post> post = postService.postByID(postId);
        if(post.isEmpty()) {
            System.out.println("Error when trying to add comment: post not found");
            return "redirect:/blog/" + postId;
        }

        Optional<User> user = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user.isEmpty()){
            System.out.println("Error when trying to add comment: user not found");
            return "redirect:/blog/" + postId;
        }

        if(full_text.isEmpty()){
            System.out.println("Error when trying to add comment: full_text is empty");
            return "redirect:/blog/" + postId;
        }


        Comment comment = new Comment(full_text);
        comment.setAuthor(user.get().getName());
        comment.setPost(post.get());
        commentService.addComment(comment);
        return "redirect:/blog/" + postId;
    }

}

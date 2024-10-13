package com.example.blog.controllers;

import com.example.blog.models.Comment;
import com.example.blog.models.Post;
import com.example.blog.models.User;
import com.example.blog.services.PostService;
import com.example.blog.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@Controller
public class MainController {

    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;

    public MainController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!auth.getName().equals("anonymousUser")) return true;
        return false;
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("auth", isAuthenticated());
        model.addAttribute("title", "О нас");
        return "about";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("auth", isAuthenticated());
        return "login";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("auth", isAuthenticated());
        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@RequestParam String name,
                               @RequestParam String password,
                               @RequestParam String passwordConfirm,
                               Model model) {
        if(postService.userByName(name).isPresent()) {
            model.addAttribute("errors", "Пользователь с таким именем уже существует");
            return "registration";
        }

        if(!password.equals(passwordConfirm)) {
            model.addAttribute("errors", "Пароли не совпадают");
            return "registration";
        }
        User user= new User(name, password, "ROLE_USER");
        postService.addUser(user);

        return "redirect:/login";
    }

    @GetMapping("/")
    public String blogMain(Model model){
        model.addAttribute("auth", isAuthenticated());
        Iterable<Post> posts = postService.allPosts();
        model.addAttribute("posts", posts);
        return "blog-main";
    }

    @GetMapping("/blog/add")
    public String blogAdd(Model model){
        model.addAttribute("auth", isAuthenticated());
        return "blog-add";
    }

    @PostMapping("/blog/add")
    public String blogPostAdd(@RequestParam String title,
                              @RequestParam String anons,
                              @RequestParam String full_text,
                              Model model ){
        Post post = new Post(title, anons, full_text);
        Optional<User> user = postService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user.isEmpty()) {
            System.out.println("Error when trying to add post: user not found");
            return "redirect:/";
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

        Optional <User> curUser = postService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(curUser.isPresent()) {
            if( curUser.get().getId().equals(post.get().getOwner().getId()) ){
                model.addAttribute("isOwn", true);
            }
            else{
                model.addAttribute("isOwn", false);
            }
        }

        Iterable<Comment> comments = commentService.commentsByPost(post.get().getId());
        model.addAttribute("comments", comments);
        return "blog-details";
    }

    @PostMapping("/blog/{id}/addComment")
    public String addComment(Model model,
                             @PathVariable(value = "id") long postId,
                             @RequestParam String full_text){
        Optional<Post> post = postService.postByID(postId);
        if(post.isEmpty()) {
            System.out.println("Error when trying to add comment: post not found");
            return "redirect:/";
        }

        Optional<User> user = postService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user.isEmpty()){
            System.out.println("Error when trying to add comment: user not found");
            return "redirect:/";
        }


        Comment comment = new Comment(full_text);
        comment.setAuthor(user.get().getName());
        commentService.addComment(comment);
        return "redirect:/blog/" + postId;
    }

    @GetMapping("/blog/{id}/edit")
    public String blogEdit(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("auth", isAuthenticated());
        Optional<Post> post = postService.postByID(id);
        if (postService.postByID(id).isEmpty()) return "redirect:/";

        ArrayList <Post> res = new ArrayList<Post>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
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

}

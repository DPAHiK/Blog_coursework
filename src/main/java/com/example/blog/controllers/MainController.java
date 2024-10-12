package com.example.blog.controllers;

import com.example.blog.models.Post;
import com.example.blog.models.User;
import com.example.blog.services.MyUserDetailsService;
import com.example.blog.services.PostService;
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
    private PostService service;

    public MainController(PostService postService) {
        this.service = postService;
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.getName().equals("anonymousUser")) return false;
        return true;
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
        if(service.userByName(name).isPresent()) {
            model.addAttribute("errors", "Пользователь с таким именем уже существует");
            return "registration";
        }

        if(!password.equals(passwordConfirm)) {
            model.addAttribute("errors", "Пароли не совпадают");
            return "registration";
        }
        User user= new User(name, password, "ROLE_USER");
        service.addUser(user);

        return "redirect:/login";
    }

    @GetMapping("/")
    public String blogMain(Model model){
        model.addAttribute("auth", isAuthenticated());
        Iterable<Post> posts = service.allPosts();
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
        Optional<User> user = service.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user.isEmpty()) {
            System.out.println("Error when trying to add post: user not found");
            return "redirect:/";
        }

        post.setOwner(user.get());
        service.addPost(post);
        return "redirect:/";
    }

    @GetMapping("/blog/{id}")
    public String blogDetails(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("auth", isAuthenticated());
        Optional<Post> post = service.postByID(id);
        if (post.isEmpty()) return "redirect:/";

        ArrayList<Post> res = new ArrayList<Post>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);

        Optional <User> curUser = service.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(curUser.isPresent()) {
            if( curUser.get().getId().equals(post.get().getOwner().getId()) ){
                model.addAttribute("isOwn", true);
            }
            else{
                model.addAttribute("isOwn", false);
            }
        }
        return "blog-details";
    }

    @GetMapping("/blog/{id}/edit")
    public String blogEdit(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("auth", isAuthenticated());
        Optional<Post> post = service.postByID(id);
        if (service.postByID(id).isEmpty()) return "redirect:/";

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
        Post post = service.postByID(id).orElseThrow();
        post.setTitle(title);
        post.setAnons(anons);
        post.setFull_text(full_text);
        service.addPost(post);

        return "redirect:/";
    }

    @PostMapping("/blog/{id}/remove")
    public String blogPostRemove(@PathVariable(value = "id") long id,
                                 Model model ){
        Post post = service.postByID(id).orElseThrow();
        service.deletePost(post);

        return "redirect:/";
    }

}

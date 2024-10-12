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

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("auth", isAuthenticated());
        model.addAttribute("title", "Главная страница");
        return "home";
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

    @GetMapping("/blog")
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
        service.addPost(post);
        return "redirect:/blog";
    }

    @GetMapping("/blog/{id}")
    public String blogDetails(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("auth", isAuthenticated());
        Optional<Post> post = service.postByID(id);
        if (service.postByID(id).isEmpty()) return "redirect:/blog";

        ArrayList<Post> res = new ArrayList<Post>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "blog-details";
    }

    @GetMapping("/blog/{id}/edit")
    public String blogEdit(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("auth", isAuthenticated());
        Optional<Post> post = service.postByID(id);
        if (service.postByID(id).isEmpty()) return "redirect:/blog";

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

        return "redirect:/blog";
    }

    @PostMapping("/blog/{id}/remove")
    public String blogPostRemove(@PathVariable(value = "id") long id,
                                 Model model ){
        Post post = service.postByID(id).orElseThrow();
        service.deletePost(post);

        return "redirect:/blog";
    }

}

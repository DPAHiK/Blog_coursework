package com.example.blog.controllers;

import com.example.blog.models.User;
import com.example.blog.services.PostService;
import com.example.blog.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        return !auth.getName().equals("anonymousUser");
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("auth", isAuthenticated());

        Optional<User> curUser = postService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                () -> model.addAttribute("curUser", null));

        return "about";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("auth", isAuthenticated());

        Optional <User> curUser = postService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                () -> model.addAttribute("curUser", null));

        return "login";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("auth", isAuthenticated());

        Optional <User> curUser = postService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                () -> model.addAttribute("curUser", null));

        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@RequestParam String name,
                               @RequestParam String password,
                               @RequestParam String passwordConfirm,
                               Model model) {
        model.addAttribute("auth", isAuthenticated());

        Optional <User> curUser = postService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                () -> model.addAttribute("curUser", null));

        if(name.isEmpty() || password.isEmpty() || name.equals("anonymousUser")) {
            model.addAttribute("errors", "Некорректные данные для регистрации. Пожалуйста, используйте другие");
            return "registration";
        }

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





}

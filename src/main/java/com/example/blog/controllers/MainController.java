package com.example.blog.controllers;

import com.example.blog.models.User;
import com.example.blog.models.UserInfo;
import com.example.blog.services.PostService;
import com.example.blog.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
        UserInfo userInfo = new UserInfo(LocalDate.now().toString(), 0L, 0L);
        userInfo.setUser(user);
        postService.addUser(user);
        postService.addUserInfo(userInfo);

        return "redirect:/login";
    }


    @GetMapping("/profile/{id}")
    public String profile(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("auth", isAuthenticated());

        Optional <User> curUser = postService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.ifPresentOrElse((user) -> model.addAttribute("curUser", user),
                () -> model.addAttribute("curUser", null));

        Optional<User> user = postService.userById(id);
        if(user.isPresent()){
            model.addAttribute("user", user.get());

        }
        else {
            System.out.println("Error with opening profile: user not found");
            return "redirect:/";
        }

        Optional<UserInfo> userInfo = postService.infoByUserID(user.get().getId());
        if(userInfo.isPresent()){
            model.addAttribute("userInfo", userInfo.get());

        }
        else {
            System.out.println("Error with opening profile: userInfo not found");
            return "redirect:/";
        }

        return "profile";
    }


}

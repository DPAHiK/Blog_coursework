package com.example.blog.controllers;

import com.example.blog.models.Post;
import com.example.blog.models.User;
import com.example.blog.models.UserInfo;
import com.example.blog.services.PostService;
import com.example.blog.services.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {

    @Autowired
    private final UserInfoService userService;
    @Autowired
    private final PostService postService;


    public MainController(UserInfoService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    private User getCurUser(){
        Optional <User> curUser = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(curUser.isPresent()) return curUser.get();

        return new User("anon","","ROLE_ANONYMOUS");
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("curUser", getCurUser());


        return "login";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("curUser", getCurUser());

        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@RequestParam String name,
                               @RequestParam String password,
                               @RequestParam String passwordConfirm,
                               Model model) {
        model.addAttribute("curUser", getCurUser());

        if(name.isEmpty() || password.isEmpty() || name.equals("anonymousUser")) {
            model.addAttribute("errors", "Некорректные данные для регистрации. Пожалуйста, используйте другие");
            return "registration";
        }

        if(userService.userByName(name).isPresent()) {
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
        userService.addUser(user);
        userService.addUserInfo(userInfo);

        return "redirect:/login";
    }


    @GetMapping("/profile/{id}")
    public String profile(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("curUser", getCurUser());



        Optional<User> user = userService.userById(id);
        if(user.isPresent()){
            model.addAttribute("user", user.get());

        }
        else {
            System.out.println("Error with opening profile: user not found");
            return "redirect:/";
        }

        Optional<UserInfo> userInfo = userService.infoByUserID(user.get().getId());
        if(userInfo.isPresent()){
            model.addAttribute("userInfo", userInfo.get());

        }
        else {
            System.out.println("Error with opening profile: userInfo not found");
            return "redirect:/";
        }

        List<Post> posts = postService.postByOwnerId(user.get().getId(), 5);
        model.addAttribute("posts", posts);

        return "profile";
    }


}

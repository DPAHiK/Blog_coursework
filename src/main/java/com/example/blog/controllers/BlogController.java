package com.example.blog.controllers;

import com.example.blog.models.Comment;
import com.example.blog.models.Post;
import com.example.blog.models.User;
import com.example.blog.models.UserInfo;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class BlogController {

    @Autowired
    private final PostService postService;
    @Autowired
    private final UserInfoService userService;
    @Autowired
    private final CommentService commentService;

    private final int pageSize = 5;


    public BlogController(PostService postService, UserInfoService userService, CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;

    }

    private User getCurUser(){
        Optional <User> curUser = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(curUser.isPresent()) return curUser.get();

        return new User("anon","","ROLE_ANONYMOUS");
    }

    @GetMapping("/")
    public String blogMain(Model model){
        model.addAttribute("curUser", getCurUser());
        model.addAttribute("curPage", 0);
        model.addAttribute("haveNext", pageSize < postService.postsCount());
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //System.out.println(auth.getName());

        Iterable<Post> posts = postService.somePosts(0, pageSize);
        model.addAttribute("posts", posts);
        return "blog-main";
    }

    @GetMapping("/{page}")
    public String blogMainNextPages(@PathVariable(value = "page") int page, Model model){
        model.addAttribute("curUser", getCurUser());
        model.addAttribute("curPage", page);
        model.addAttribute("haveNext", (page + 1) * pageSize  < postService.postsCount());
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //System.out.println(auth.getName());

        List<Post> posts = postService.somePosts(page * pageSize, pageSize);

        if(posts.isEmpty()) return "redirect:/";

        model.addAttribute("posts", posts);
        return "blog-main";
    }

    @GetMapping("/search")
    public String blogSearch(@BindParam String postName, Model model){
        if(postName.isBlank()) return "redirect:/";

        model.addAttribute("curUser", getCurUser());
        model.addAttribute("curPage", 0);
        model.addAttribute("postName", postName);
        model.addAttribute("haveNext", pageSize < postService.postsCount(postName));
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //System.out.println(auth.getName());

        Iterable<Post> posts = postService.somePosts(0, pageSize, postName);
        model.addAttribute("posts", posts);
        return "blog-search";
    }

    @GetMapping("/search/{page}")
    public String blogSearchNextPages(@BindParam String postName, @PathVariable(value = "page") int page, Model model){
        System.out.println(postName);

        if(postName.isBlank()) return "redirect:/";

        model.addAttribute("curUser", getCurUser());
        model.addAttribute("curPage", page);
        model.addAttribute("postName", postName);
        model.addAttribute("haveNext", (page + 1) * pageSize  < postService.postsCount(postName));
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //System.out.println(auth.getName());

        List<Post> posts = postService.somePosts(page * pageSize, pageSize, postName);

        if(posts.isEmpty()) return "redirect:/";

        model.addAttribute("posts", posts);
        return "blog-search";
    }

    @GetMapping("/add/blog")
    public String blogAdd(Model model){
        model.addAttribute("curUser", getCurUser());

        return "blog-add";
    }

    @PostMapping("/add/blog")
    public String blogPostAdd(@RequestParam String title,
                              @RequestParam String anons,
                              @RequestParam String full_text,
                              Model model ){
        model.addAttribute("curUser", getCurUser());

        if(title.isEmpty() || anons.isEmpty() || full_text.isEmpty()) {
            model.addAttribute("errors", "Все поля должны быть заполнены");
            return "blog-add";
        }

        Post post = new Post(title, anons, full_text, LocalDate.now().toString());
        Optional<User> user = userService.userByName(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user.isEmpty()) {
            model.addAttribute("errors", "Ошибка при создании поста: пользователь не найден");
            System.out.println("Error when trying to add post: user not found");
            return "blog-add";
        }

        post.setOwner(user.get());
        postService.addPost(post);

        Optional<UserInfo> userInfo = userService.infoByUserID(user.get().getId());
        userInfo.ifPresent(info -> {info.setPostCount(info.getPostCount() + 1);
                                    userService.addUserInfo(info);});

        return "redirect:/";
    }

    @GetMapping("/blog/{id}")
    public String blogDetails(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("curUser", getCurUser());

        Optional<Post> post = postService.postByID(id);
        if (post.isEmpty()) return "redirect:/";
        model.addAttribute("post", post.get());

        Optional <User> owner = userService.userById(post.get().getOwner().getId());
        owner.ifPresentOrElse((user) -> model.addAttribute("owner", user),
                () -> model.addAttribute("owner", new User("anon","","ROLE_ANONYMOUS")));

        Iterable<Comment> comments = commentService.commentsByPost(post.get().getId());
        model.addAttribute("comments", comments);
        return "blog-details";
    }



    @GetMapping("/blog/{id}/edit")
    public String blogEdit(Model model, @PathVariable(value = "id") long id){
        model.addAttribute("curUser", getCurUser());

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
        User owner = userService.userById(post.getOwner().getId()).orElseThrow();
        postService.deletePost(post);

        Optional<UserInfo> userInfo = userService.infoByUserID(owner.getId());
        userInfo.ifPresent(info -> {info.setPostCount(info.getPostCount() - 1);
            userService.addUserInfo(info);});

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
        comment.setAuthor(user.get());
        comment.setPost(post.get());
        commentService.addComment(comment);

        Optional<UserInfo> userInfo = userService.infoByUserID(user.get().getId());
        userInfo.ifPresent(info -> {info.setCommentCount(info.getCommentCount() + 1);
            userService.addUserInfo(info);});

        return "redirect:/blog/" + postId;
    }

}

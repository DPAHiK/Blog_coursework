package com.example.blog.controllers;

import com.example.blog.models.Comment;
import com.example.blog.models.User;
import com.example.blog.models.UserInfo;
import com.example.blog.services.CommentService;
import com.example.blog.services.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@Controller
public class CommentController {
    @Autowired
    private final CommentService commentService;
    @Autowired
    private final UserInfoService userService;

    public CommentController(CommentService commentService, UserInfoService userService) {

        this.commentService = commentService;
        this.userService = userService;
    }

    @PostMapping("/blog/{id}/editComment/{id_comment}")
    public String editComment(Model model,
                              @PathVariable(value = "id") long postId,
                              @PathVariable(value = "id_comment") long commentId,
                              @RequestParam String full_text){
        Optional<Comment> comment = commentService.commentByID(commentId);
        if(comment.isEmpty()) {
            System.out.println("Error when trying to edit comment: comment not found");
            return "redirect:/blog/" + postId;
        }
        comment.get().setFull_text(full_text);
        commentService.addComment(comment.get());

        return "redirect:/blog/" + postId;
    }

    @PostMapping("/blog/{id}/removeComment/{id_comment}")
    public String removeComment(Model model,
                                @PathVariable(value = "id") long postId,
                                @PathVariable(value = "id_comment") long commentId){
        Comment comment = commentService.commentByID(commentId).orElseThrow();
        User author = userService.userByName(comment.getAuthor().getName()).orElseThrow();
        commentService.deleteComment(comment);

        Optional<UserInfo> userInfo = userService.infoByUserID(author.getId());
        userInfo.ifPresent(info -> {info.setCommentCount(info.getCommentCount() - 1);
            userService.addUserInfo(info);});

        return "redirect:/blog/" + postId;
    }
}

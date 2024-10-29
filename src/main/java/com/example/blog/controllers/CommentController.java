package com.example.blog.controllers;

import com.example.blog.models.Comment;
import com.example.blog.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@Controller
public class CommentController {
    @Autowired
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
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
        Optional<Comment> comment = commentService.commentByID(commentId);
        if(comment.isEmpty()) {
            System.out.println("Error when trying to edit comment: comment not found");
            return "redirect:/blog/" + postId;
        }
        commentService.deleteComment(comment.get());
        return "redirect:/blog/" + postId;
    }
}

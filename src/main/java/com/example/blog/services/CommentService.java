package com.example.blog.services;

import com.example.blog.repo.CommentRepository;
import org.springframework.stereotype.Service;
import com.example.blog.models.Comment;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {     
    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> allComments() {
        return (List<Comment>) commentRepository.findAll();
    }

    public Optional<Comment> commentByID(long id) {
        return commentRepository.findById(id);
    }

    public List<Comment> commentsByPost(long id) {
        return (List<Comment>) commentRepository.findByPostId(id);
    }


    public void addComment(Comment comment) {
        commentRepository.save(comment);
    }

    public void deleteComment(Comment comment) {
        commentRepository.delete(comment);
    }
}

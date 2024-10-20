package com.example.blog.services;

import com.example.blog.repo.CommentRepository;
import org.springframework.stereotype.Service;
import com.example.blog.models.Comment;

import java.util.Optional;

@Service
public class CommentService {       //Он вообще нужен?
    private Iterable<Comment> comments;
    private CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
        comments = commentRepository.findAll();
    }

    public Iterable<Comment> allComments() {
        return comments;
    }

    public Optional<Comment> commentByID(long id) {
        Optional<Comment> comment = Optional.empty();
        for (Comment comment1 : comments) {
            if (comment1.getId() == id) {
                comment = Optional.of(comment1);
            }
        }
        return comment;
    }

    public Iterable<Comment> commentsByPost(long id) {
        return commentRepository.findByPostId(id);
    }


    public void addComment(Comment post) {
        commentRepository.save(post);
        this.comments = commentRepository.findAll(); // мб получше что-то?
    }

    public void deleteComment(Comment comment) {
        commentRepository.delete(comment);
        this.comments = commentRepository.findAll();
    }
}

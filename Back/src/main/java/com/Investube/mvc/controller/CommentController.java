package com.Investube.mvc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Investube.mvc.model.dto.BoardComment;
import com.Investube.mvc.model.service.CommentService;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 조회
    @GetMapping("/board/{postId}/comments")
    public ResponseEntity<List<BoardComment>> getComments(@PathVariable int postId) {
        List<BoardComment> list = commentService.getCommentsByPostId(postId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // 댓글 작성
    @PostMapping("/board/{postId}/comments")
    public ResponseEntity<Integer> addComment(
            @PathVariable int postId,
            @RequestBody BoardComment comment) {

        comment.setPostId(postId);
        int result = commentService.insertComment(comment);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    // 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Integer> updateComment(
            @PathVariable int commentId,
            @RequestBody BoardComment comment) {

        comment.setCommentId(commentId);
        int result = commentService.updateComment(comment);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Integer> deleteComment(@PathVariable int commentId) {
        int result = commentService.deleteComment(commentId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

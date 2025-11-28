package com.Investube.mvc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Investube.mvc.model.dto.BoardComment;
import com.Investube.mvc.model.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "댓글 API", description = "게시글 댓글 작성, 조회, 수정, 삭제")
@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 조회
    @Operation(summary = "게시글 댓글 목록 조회", description = "특정 게시글의 모든 댓글을 조회합니다")
    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공")
    @GetMapping("/board/{postId}/comments")
    public ResponseEntity<List<BoardComment>> getComments(@Parameter(description = "게시글 ID") @PathVariable int postId) {
        List<BoardComment> list = commentService.getCommentsByPostId(postId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // 댓글 작성
    @Operation(summary = "댓글 작성", description = "게시글에 새로운 댓글을 작성합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/board/{postId}/comments")
    public ResponseEntity<Integer> addComment(
            @Parameter(description = "게시글 ID") @PathVariable int postId,
            @Parameter(description = "댓글 정보") @RequestBody BoardComment comment) {

        comment.setPostId(postId);
        int result = commentService.insertComment(comment);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    // 댓글 수정
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Integer> updateComment(
            @Parameter(description = "댓글 ID") @PathVariable int commentId,
            @Parameter(description = "수정할 댓글 정보") @RequestBody BoardComment comment) {

        comment.setCommentId(commentId);
        int result = commentService.updateComment(comment);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 댓글 삭제
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Integer> deleteComment(@Parameter(description = "댓글 ID") @PathVariable int commentId) {
        int result = commentService.deleteComment(commentId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

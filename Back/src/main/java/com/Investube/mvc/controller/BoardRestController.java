package com.Investube.mvc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Investube.mvc.model.dto.BoardImage;
import com.Investube.mvc.model.dto.BoardPost;
import com.Investube.mvc.model.service.BoardService;

@RestController
@RequestMapping("/board")
public class BoardRestController {

    private final BoardService boardService;

    public BoardRestController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 게시글 목록 + 검색
    @GetMapping
    public ResponseEntity<List<BoardPost>> getBoardList(
            @RequestParam(required = false) String keyword) {

        List<BoardPost> list = boardService.getBoardList(keyword);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // 게시글 상세
    @GetMapping("/{postId}")
    public ResponseEntity<BoardPost> getPost(@PathVariable int postId) {
        BoardPost post = boardService.getPostById(postId);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    // 게시글 작성
    @PostMapping
    public ResponseEntity<Integer> createPost(@RequestBody BoardPost post) {
        int result = boardService.createPost(post);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<Integer> updatePost(
            @PathVariable int postId,
            @RequestBody BoardPost post) {

        post.setPostId(postId);
        int result = boardService.updatePost(post);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Integer> deletePost(@PathVariable int postId) {
        int result = boardService.deletePost(postId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 이미지 조회
    @GetMapping("/{postId}/images")
    public ResponseEntity<List<BoardImage>> getImages(@PathVariable int postId) {
        List<BoardImage> list = boardService.getImagesByPostId(postId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

}

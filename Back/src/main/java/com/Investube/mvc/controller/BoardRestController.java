package com.Investube.mvc.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.Investube.mvc.model.dto.BoardImage;
import com.Investube.mvc.model.dto.BoardPost;
import com.Investube.mvc.model.service.BoardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "게시판 API", description = "게시판 CRUD 및 이미지 업로드")
@RestController
@RequestMapping("/board")
public class BoardRestController {

    private final BoardService boardService;
    private static final String UPLOAD_DIR = "uploads/board/";

    public BoardRestController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 게시글 목록 + 검색
    @Operation(summary = "게시글 목록 조회", description = "키워드로 게시글 검색 가능")
    @GetMapping
    public ResponseEntity<List<BoardPost>> getBoardList(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword) {

        List<BoardPost> list = boardService.getBoardList(keyword);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // 게시글 상세
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다")
    @GetMapping("/{postId}")
    public ResponseEntity<BoardPost> getPost(
            @Parameter(description = "게시글 ID") @PathVariable int postId) {
        BoardPost post = boardService.getPostById(postId);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    // 게시글 작성 (파일 업로드 포함)
    @Operation(summary = "게시글 작성", description = "이미지 파일 업로드 포함 (multipart/form-data)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createPost(
            @Parameter(description = "게시글 제목") @RequestParam("title") String title,
            @Parameter(description = "게시글 내용") @RequestParam("content") String content,
            @Parameter(description = "작성자 ID") @RequestParam("userId") int userId,
            @Parameter(description = "이미지 파일들 (다중 업로드 가능)") @RequestParam(value = "images", required = false) List<MultipartFile> files) throws IOException {

        // 1. 게시글 등록
        BoardPost post = new BoardPost();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        
        int result = boardService.createPost(post);
        
        if (result == 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "게시글 작성 실패");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // 2. 이미지 파일 저장
        if (files != null && !files.isEmpty()) {
            List<BoardImage> images = new ArrayList<>();
            
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String savedPath = saveFile(file);
                    
                    BoardImage img = new BoardImage();
                    img.setPostId(post.getPostId());
                    img.setImageUrl(savedPath);
                    images.add(img);
                }
            }
            
            if (!images.isEmpty()) {
                boardService.insertImages(images);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("postId", post.getPostId());
        response.put("message", "게시글이 작성되었습니다.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    // 파일 저장 메서드
    private String saveFile(MultipartFile file) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일명 생성 (중복 방지)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(fileName);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 반환할 URL 경로
        return "/" + UPLOAD_DIR + fileName;
    }

    // 게시글 수정
    @Operation(summary = "게시글 수정", description = "게시글의 제목과 내용을 수정합니다")
    @PutMapping("/{postId}")
    public ResponseEntity<Integer> updatePost(
            @Parameter(description = "게시글 ID") @PathVariable int postId,
            @RequestBody BoardPost post) {

        post.setPostId(postId);
        int result = boardService.updatePost(post);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 게시글 삭제
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Integer> deletePost(
            @Parameter(description = "게시글 ID") @PathVariable int postId) {
        int result = boardService.deletePost(postId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}

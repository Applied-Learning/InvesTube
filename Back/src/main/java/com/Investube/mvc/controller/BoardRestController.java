package com.Investube.mvc.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Investube.mvc.model.dto.BoardPost;
import com.Investube.mvc.model.service.BoardService;
import com.Investube.mvc.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "게시판 API", description = "게시판 CRUD 및 이미지 업로드")
@RestController
@RequestMapping("/boards")
public class BoardRestController {

    private final BoardService boardService;
    private final JwtUtil jwtUtil;
    private static final String UPLOAD_DIR = "uploads/board/";

    public BoardRestController(BoardService boardService, JwtUtil jwtUtil) {
        this.boardService = boardService;
        this.jwtUtil = jwtUtil;
    }
    
    // JWT에서 userId 추출
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUserIdByToken(token);
        }
        return null;
    }

    // 게시글 목록 + 검색 + 페이징 + 정렬
    @Operation(summary = "게시글 목록 조회", description = "검색어, 정렬, 페이지 정보를 이용해 게시글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBoardList(
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬 기준 (latest / views)") @RequestParam(defaultValue = "latest") String sortBy,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

        int offset = page * size;
        List<BoardPost> posts = boardService.getBoardList(keyword, sortBy, offset, size);
        int totalCount = boardService.getTotalCount(keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts);
        response.put("totalCount", totalCount);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 게시글 상세
    @Operation(summary = "게시글 상세 조회", description = "지정한 게시글의 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<BoardPost> getPost(
            @Parameter(description = "게시글 ID") @PathVariable int postId) {
        try {
            boardService.increaseViewCount(postId);
        } catch (Exception e) {
            // 조회수 증가 실패는 조회 자체를 막지 않음
        }

        BoardPost post = boardService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    // 게시글 생성 (제목/내용만, 이미지는 에디터 업로드 API 사용)
    @Operation(summary = "게시글 생성", description = "제목과 내용으로 게시글을 생성합니다. 이미지는 별도 업로드 API를 사용합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createPost(
            @Parameter(description = "게시글 제목") @RequestParam("title") String title,
            @Parameter(description = "게시글 내용") @RequestParam("content") String content,
            HttpServletRequest request) throws IOException {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ResponseEntity<>(Map.of("message", "인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }

        BoardPost post = new BoardPost();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        
        int result = boardService.createPost(post);
        
        if (result == 0) {
            return new ResponseEntity<>(Map.of("message", "게시글 생성에 실패했습니다."), HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("postId", post.getPostId());
        response.put("message", "게시글이 생성되었습니다.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    // 파일 저장 유틸
    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_" + (originalFilename != null ? originalFilename : "file");
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/" + UPLOAD_DIR + fileName;
    }

    // 게시글 수정
    @Operation(summary = "게시글 수정", description = "게시글의 제목과 내용을 수정합니다.")
    @PutMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> updatePost(
            @Parameter(description = "게시글 ID") @PathVariable int postId,
            @RequestBody BoardPost post,
            HttpServletRequest request) {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ResponseEntity<>(Map.of("message", "인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        
        BoardPost existingPost = boardService.getPostById(postId);
        if (existingPost == null) {
            return new ResponseEntity<>(Map.of("message", "게시글을 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
        }
        if (existingPost.getUserId() != userId) {
            return new ResponseEntity<>(Map.of("message", "본인 작성 게시글만 수정할 수 있습니다."), HttpStatus.FORBIDDEN);
        }

        post.setPostId(postId);
        int result = boardService.updatePost(post);
        return new ResponseEntity<>(Map.of("message", "게시글이 수정되었습니다.", "result", result), HttpStatus.OK);
    }

    // 게시글 삭제
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(
            @Parameter(description = "게시글 ID") @PathVariable int postId,
            HttpServletRequest request) {
        
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ResponseEntity<>(Map.of("message", "인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }
        
        BoardPost existingPost = boardService.getPostById(postId);
        if (existingPost == null) {
            return new ResponseEntity<>(Map.of("message", "게시글을 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
        }
        if (existingPost.getUserId() != userId) {
            return new ResponseEntity<>(Map.of("message", "본인 작성 게시글만 삭제할 수 있습니다."), HttpStatus.FORBIDDEN);
        }
        
        int result = boardService.deletePost(postId);
        return new ResponseEntity<>(Map.of("message", "게시글이 삭제되었습니다.", "result", result), HttpStatus.OK);
    }
    
    // 사용자별 게시글 조회
    @Operation(summary = "사용자별 게시글 조회", description = "특정 사용자가 작성한 게시글 목록을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getPostsByUser(
            @Parameter(description = "사용자 ID") @PathVariable int userId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        int offset = page * size;
        List<BoardPost> posts = boardService.getPostsByUserId(userId, offset, size);
        int totalCount = boardService.getPostCountByUserId(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts);
        response.put("totalCount", totalCount);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 에디터 전용: postId 없이 단일 이미지 업로드
    @Operation(summary = "게시판 이미지 업로드", description = "에디터에서 사용하는 단일 이미지 업로드 API입니다.")
    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadEditorImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ResponseEntity<>(Map.of("message", "인증 정보가 없습니다."), HttpStatus.UNAUTHORIZED);
        }

        if (file == null || file.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "업로드할 이미지가 없습니다."), HttpStatus.BAD_REQUEST);
        }

        String savedPath = saveFile(file);

        Map<String, Object> res = new HashMap<>();
        res.put("url", savedPath);
        res.put("message", "이미지가 업로드되었습니다.");

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}


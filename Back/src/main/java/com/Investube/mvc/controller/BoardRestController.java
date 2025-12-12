package com.Investube.mvc.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.Investube.mvc.model.dto.BoardImage;
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
    @Operation(summary = "게시글 목록 조회", description = "키워드로 게시글 검색 가능 (페이징, 정렬 지원)")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBoardList(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬 기준 (latest: 최신순, views: 조회수순)") @RequestParam(defaultValue = "latest") String sortBy,
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
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다")
    @GetMapping("/{postId}")
    public ResponseEntity<BoardPost> getPost(
            @Parameter(description = "게시글 ID") @PathVariable int postId) {
        // 조회수 증가
        try {
            boardService.increaseViewCount(postId);
        } catch (Exception e) {
            // 로그만 남기고 조회는 계속 진행
            // (avoid failing the request due to view count update issue)
        }

        BoardPost post = boardService.getPostById(postId);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    // 게시글 작성 (파일 업로드 포함)
    @Operation(summary = "게시글 작성", description = "이미지 파일 업로드 포함 (multipart/form-data)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createPost(
            @Parameter(description = "게시글 제목") @RequestParam("title") String title,
            @Parameter(description = "게시글 내용") @RequestParam("content") String content,
            @Parameter(description = "이미지 파일들 (다중 업로드 가능)") @RequestParam(value = "images", required = false) List<MultipartFile> files,
            HttpServletRequest request) throws IOException {

        // JWT에서 userId 추출
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ResponseEntity<>(Map.of("message", "인증이 필요합니다"), HttpStatus.UNAUTHORIZED);
        }

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
    public ResponseEntity<Map<String, Object>> updatePost(
            @Parameter(description = "게시글 ID") @PathVariable int postId,
            @RequestBody BoardPost post,
            HttpServletRequest request) {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ResponseEntity<>(Map.of("message", "인증이 필요합니다"), HttpStatus.UNAUTHORIZED);
        }
        
        // 작성자 확인
        BoardPost existingPost = boardService.getPostById(postId);
        if (existingPost == null) {
            return new ResponseEntity<>(Map.of("message", "게시글을 찾을 수 없습니다"), HttpStatus.NOT_FOUND);
        }
        if (existingPost.getUserId() != userId) {
            return new ResponseEntity<>(Map.of("message", "본인이 작성한 게시글만 수정할 수 있습니다"), HttpStatus.FORBIDDEN);
        }

        post.setPostId(postId);
        int result = boardService.updatePost(post);
        return new ResponseEntity<>(Map.of("message", "게시글이 수정되었습니다", "result", result), HttpStatus.OK);
    }

    // 게시글에 이미지 추가 (편집 중 이미지 업로드)
    @Operation(summary = "게시글 이미지 추가", description = "게시글에 이미지를 추가합니다")
    @PostMapping(value = "/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> addImages(
            @Parameter(description = "게시글 ID") @PathVariable int postId,
            @Parameter(description = "이미지 파일들") @RequestParam(value = "images", required = false) List<MultipartFile> files,
            HttpServletRequest request) throws IOException {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ResponseEntity<>(Map.of("message", "인증이 필요합니다"), HttpStatus.UNAUTHORIZED);
        }

        BoardPost post = boardService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(Map.of("message", "게시글을 찾을 수 없습니다"), HttpStatus.NOT_FOUND);
        }
        if (post.getUserId() != userId) {
            return new ResponseEntity<>(Map.of("message", "본인이 작성한 게시글만 수정할 수 있습니다"), HttpStatus.FORBIDDEN);
        }

        if (files != null && !files.isEmpty()) {
            List<BoardImage> images = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String savedPath = saveFile(file);
                    BoardImage img = new BoardImage();
                    img.setPostId(postId);
                    img.setImageUrl(savedPath);
                    images.add(img);
                }
            }
            if (!images.isEmpty()) {
                boardService.insertImages(images);
            }
        }

        return new ResponseEntity<>(Map.of("message", "이미지가 추가되었습니다"), HttpStatus.OK);
    }

    // 게시글 이미지 삭제
    @Operation(summary = "게시글 이미지 삭제", description = "단일 게시글 이미지를 삭제합니다")
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Map<String, Object>> deleteImage(
            @Parameter(description = "이미지 ID") @PathVariable int imageId,
            HttpServletRequest request) {

        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ResponseEntity<>(Map.of("message", "인증이 필요합니다"), HttpStatus.UNAUTHORIZED);
        }

        try {
            BoardImage img = boardService.getImageById(imageId);
            if (img == null) {
                return new ResponseEntity<>(Map.of("message", "이미지를 찾을 수 없습니다"), HttpStatus.NOT_FOUND);
            }

            BoardPost post = boardService.getPostById(img.getPostId());
            if (post == null || post.getUserId() != userId) {
                return new ResponseEntity<>(Map.of("message", "삭제 권한이 없습니다"), HttpStatus.FORBIDDEN);
            }

            // 파일 삭제 (시도하되 실패해도 DB 레코드 삭제는 시도)
            String url = img.getImageUrl();
            if (url != null && !url.isBlank()) {
                try {
                    // URL 예: /uploads/board/12345_file.jpg 또는 uploads/board/12345_file.jpg
                    String relative = url.startsWith("/") ? url.substring(1) : url;
                    Path filePath = Paths.get(relative).normalize();
                    // 안전성: 경로가 uploads 폴더 내부인지 확인
                    if (filePath.startsWith(UPLOAD_DIR.replace("/", "")) || filePath.startsWith(UPLOAD_DIR)) {
                        try {
                            Files.deleteIfExists(filePath);
                        } catch (NoSuchFileException nsfe) {
                            // 파일이 없으면 무시
                        }
                    } else {
                        // 업로드 디렉토리 외부 경로 삭제 시도 방지 (무시)
                    }
                } catch (Exception ex) {
                    // 파일 삭제 실패는 무시하고 진행
                }
            }

            int result = boardService.deleteImage(imageId);
            if (result > 0) {
                return new ResponseEntity<>(Map.of("message", "이미지가 삭제되었습니다", "result", result), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of("message", "이미지 삭제에 실패했습니다 (DB)", "result", result), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "서버 오류: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 게시글 삭제
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(
            @Parameter(description = "게시글 ID") @PathVariable int postId,
            HttpServletRequest request) {
        
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ResponseEntity<>(Map.of("message", "인증이 필요합니다"), HttpStatus.UNAUTHORIZED);
        }
        
        // 작성자 확인
        BoardPost existingPost = boardService.getPostById(postId);
        if (existingPost == null) {
            return new ResponseEntity<>(Map.of("message", "게시글을 찾을 수 없습니다"), HttpStatus.NOT_FOUND);
        }
        if (existingPost.getUserId() != userId) {
            return new ResponseEntity<>(Map.of("message", "본인이 작성한 게시글만 삭제할 수 있습니다"), HttpStatus.FORBIDDEN);
        }
        
        int result = boardService.deletePost(postId);
        return new ResponseEntity<>(Map.of("message", "게시글이 삭제되었습니다", "result", result), HttpStatus.OK);
    }
    
    // 사용자별 게시글 조회
    @Operation(summary = "사용자별 게시글 조회", description = "특정 사용자가 작성한 게시글 목록을 조회합니다")
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

}

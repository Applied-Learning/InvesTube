package com.Investube.mvc.controller;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.Investube.mvc.model.dto.User;
import com.Investube.mvc.model.dto.Video;
import com.Investube.mvc.model.service.ReviewService;
import com.Investube.mvc.model.service.UserService;
import com.Investube.mvc.model.service.VideoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "사용자 API", description = "사용자 정보 관리 및 프로필 이미지 업로드")
@RestController
@RequestMapping("/users")
public class UserRestController {


    private final UserService userService;
    private static final String UPLOAD_DIR = "uploads/user/";

	private final VideoService videoService;
	private final ReviewService reviewService;


	public UserRestController(UserService userService, VideoService videoService, ReviewService reviewService) {
		this.userService = userService;
		this.videoService = videoService;
		this.reviewService = reviewService;
	}


    // 업로더 정보 조회 (공개 프로필)
    @Operation(summary = "사용자 정보 조회", description = "특정 사용자의 공개 프로필 정보를 조회합니다")
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@Parameter(description = "사용자 ID") @PathVariable("userId") int userId) {
        User result = userService.getUserByUserId(userId);


		if (result == null) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

    // 내 정보 조회
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<User> getMyInfo(HttpServletRequest request) {


		int userId = (int) request.getAttribute("userId"); // JWT에서 가져옴

		User result = userService.getMyInfo(userId);

		if (result == null) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}


    // 내 정보 수정 (닉네임, 프로필 이미지)
    @Operation(summary = "내 정보 수정", description = "닉네임 및 프로필 이미지를 수정합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    public ResponseEntity<Integer> updateMyInfo(HttpServletRequest request,
                                                @RequestBody User user) {

		int userId = (int) request.getAttribute("userId");
		user.setUserId(userId);

		int result = userService.updateMyInfo(user);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}


    // 비밀번호 변경
    @Operation(summary = "비밀번호 변경", description = "현재 사용자의 비밀번호를 변경합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me/password")
    public ResponseEntity<Integer> updatePassword(HttpServletRequest request,
                                                  @Parameter(description = "새 비밀번호") @RequestParam String password) {


		int userId = (int) request.getAttribute("userId");

		int result = userService.updatePassword(userId, password);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}


    // 회원 탈퇴
    @Operation(summary = "회원 탈퇴", description = "현재 사용자의 계정을 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    public ResponseEntity<Integer> deleteUser(HttpServletRequest request) {


		int userId = (int) request.getAttribute("userId");

		int result = userService.deleteUser(userId);


        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 프로필 이미지 삭제
    @Operation(summary = "프로필 이미지 삭제", description = "사용자의 프로필 이미지를 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<Map<String, Object>> deleteProfileImage(HttpServletRequest request) {
        
        int userId = (int) request.getAttribute("userId");
        
        // 현재 프로필 이미지 정보 조회
        User currentUser = userService.getMyInfo(userId);
        
        if (currentUser == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "사용자를 찾을 수 없습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
        
        // 기존 프로필 이미지 파일 삭제
        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            deleteFile(currentUser.getProfileImage());
        }
        
        // 데이터베이스에서 프로필 이미지 NULL로 업데이트
        int result = userService.deleteProfileImage(userId);
        
        if (result > 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "프로필 이미지가 삭제되었습니다.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "프로필 이미지 삭제에 실패했습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 프로필 이미지 업로드
    @Operation(summary = "프로필 이미지 업로드", description = "사용자의 프로필 이미지를 업로드합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            HttpServletRequest request,
            @Parameter(description = "업로드할 이미지 파일") @RequestParam("image") MultipartFile file) {

        try {
            int userId = (int) request.getAttribute("userId");

            if (file.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "파일이 선택되지 않았습니다.");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // 이미지 파일인지 확인
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "이미지 파일만 업로드 가능합니다.");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // 기존 프로필 이미지가 있다면 삭제
            User currentUser = userService.getMyInfo(userId);
            if (currentUser != null && currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                deleteFile(currentUser.getProfileImage());
            }

            // 파일 저장
            String savedPath = saveFile(file);

            // 사용자 프로필 이미지 업데이트
            User user = new User();
            user.setUserId(userId);
            user.setProfileImage(savedPath);

            int result = userService.updateMyInfo(user);

            if (result > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "프로필 이미지가 업로드되었습니다.");
                response.put("profileImage", savedPath);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "프로필 이미지 업데이트에 실패했습니다.");
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "파일 업로드 중 오류가 발생했습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    // 파일 삭제 메서드
    private void deleteFile(String filePath) {
        try {
            if (filePath != null && filePath.startsWith("/")) {
                // URL 경로에서 실제 파일 경로로 변환 (맨 앞의 / 제거)
                String actualPath = filePath.substring(1);
                Path path = Paths.get(actualPath);
                
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            }
        } catch (IOException e) {
            // 파일 삭제 실패 시 로그만 남기고 계속 진행
            System.err.println("파일 삭제 실패: " + filePath + ", 오류: " + e.getMessage());
        }
    }

		
	
	
	// 내가 업로드한 영상
	@GetMapping("/me/videos")
	public ResponseEntity<List<Video>> getMyVideos(HttpServletRequest request) {
	    int userId = (int) request.getAttribute("userId");
	    
	    List<Video> result = videoService.getVideosByUser(userId);
	    return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// 내가 찜한 영상
	@GetMapping("/me/wishlist")
	public ResponseEntity<?> getMyWishlist(HttpServletRequest request) {
	    int userId = (int) request.getAttribute("userId");
	    return ResponseEntity.ok(videoService.getWishedVideos(userId));
	}

	// 내가 쓴 리뷰
	@GetMapping("/me/reviews")
	public ResponseEntity<?> getMyReviews(HttpServletRequest request) {
	    int userId = (int) request.getAttribute("userId");
	    return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
	}

}

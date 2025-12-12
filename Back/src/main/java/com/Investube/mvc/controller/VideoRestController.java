package com.Investube.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
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

import com.Investube.mvc.model.dto.Video;
import com.Investube.mvc.model.service.VideoService;
import com.Investube.mvc.model.service.NotificationService;
import com.Investube.mvc.model.service.UserService;
import com.Investube.mvc.model.dto.Notification;
import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "동영상 API", description = "동영상 조회, 등록, 수정, 삭제 및 찜 기능")
@RestController
@RequestMapping("/videos")
public class VideoRestController {
	
	private final VideoService videoService;
	private final NotificationService notificationService;
	private final UserService userService;
    
	public VideoRestController(VideoService videoService, NotificationService notificationService, UserService userService) {
		this.videoService = videoService;
		this.notificationService = notificationService;
		this.userService = userService;
	}
	
	// 전체 비디오 조회 (정렬 기능 + 페이징)
	@Operation(summary = "동영상 목록 조회", description = "전체 동영상을 조회합니다. 정렬 및 페이징 지원")
	@ApiResponse(responseCode = "200", description = "동영상 목록 조회 성공")
	@GetMapping
	public ResponseEntity<Map<String, Object>> getAllVideos(
			@Parameter(description = "정렬 기준 (latest, views, rating)", example = "latest") 
			@RequestParam(required = false, defaultValue = "latest") String sortBy,
			@Parameter(description = "페이지 번호", example = "1") 
			@RequestParam(required = false) Integer page,
			@Parameter(description = "페이지 크기", example = "10") 
			@RequestParam(required = false) Integer size) {
		
		// 페이징 파라미터가 없으면 기존 방식대로 전체 조회
		if (page == null || size == null) {
			List<Video> videos;
			switch (sortBy) {
				case "views":
					videos = videoService.getVideosByViews();
					break;
				case "rating":
					videos = videoService.getVideosByRating();
					break;
				case "latest":
				default:
					videos = videoService.getAllVideos();
					break;
			}
			
			Map<String, Object> response = new HashMap<>();
			response.put("videos", videos);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		
		// 페이징 파라미터 검증
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;
		
		// offset 계산
		int offset = (page - 1) * size;
		
		// 페이징 처리
		List<Video> videos;
		int totalCount = videoService.getTotalVideoCount();
		
		switch (sortBy) {
			case "views":
				videos = videoService.getVideosByViews(offset, size);
				break;
			case "rating":  
				videos = videoService.getVideosByRating(offset, size);
				break;
			case "latest":
			default:
				videos = videoService.getAllVideos(offset, size);
				break;
		}
		
		// 페이징 정보 포함한 응답
		Map<String, Object> response = new HashMap<>();
		response.put("videos", videos);
		response.put("currentPage", page);
		response.put("pageSize", size);
		response.put("totalCount", totalCount);
		response.put("totalPages", (int) Math.ceil((double) totalCount / size));
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	// 비디오 ID로 조회 (영상 상세 조회)
	@Operation(summary = "동영상 상세 조회", description = "동영상 ID로 특정 동영상의 상세 정보를 조회합니다. 조회수가 1 증가합니다")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "동영상 조회 성공"),
		@ApiResponse(responseCode = "404", description = "동영상을 찾을 수 없음")
	})
	@GetMapping("/{videoId}")
	public ResponseEntity<Video> getVideo(@Parameter(description = "동영상 ID") @PathVariable int videoId) {
		Video video = videoService.getVideo(videoId);
		if (video != null) {
			// 조회수 증가
			videoService.increaseViewCount(videoId);
			return new ResponseEntity<>(video, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// 비디오 등록 (인증 필요)
	@Operation(summary = "동영상 등록", description = "새로운 동영상을 등록합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "동영상 등록 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping
	public ResponseEntity<Void> createVideo(@Parameter(description = "등록할 동영상 정보") @RequestBody Video video, HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		video.setUserId(userId);
		if (videoService.createVideo(video)) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 비디오 수정 (인증 + 소유자 검증)
	@Operation(summary = "동영상 수정", description = "동영상 정보를 수정합니다. 본인이 등록한 동영상만 수정 가능합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "동영상 수정 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "동영상을 찾을 수 없음")
	})
	@PutMapping("/{videoId}")
	public ResponseEntity<Void> updateVideo(@Parameter(description = "동영상 ID") @PathVariable int videoId, 
											@Parameter(description = "수정할 동영상 정보") @RequestBody Video video, 
											HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		Video existing = videoService.getVideo(videoId);
		if (existing == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (existing.getUserId() != userId) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		video.setVideoId(videoId);
		video.setUserId(userId);
		if (videoService.modifyVideo(video)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 비디오 삭제 (인증 + 소유자 검증)
	@Operation(summary = "동영상 삭제", description = "동영상을 삭제합니다. 본인이 등록한 동영상만 삭제 가능합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "동영상 삭제 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "동영상을 찾을 수 없음")
	})
	@DeleteMapping("/{videoId}")
	public ResponseEntity<Void> deleteVideo(@Parameter(description = "동영상 ID") @PathVariable int videoId, HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		Video existing = videoService.getVideo(videoId);
		if (existing == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (existing.getUserId() != userId) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		if (videoService.removeVideo(videoId)) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 카테고리별 비디오 조회 (페이징 지원)
	@Operation(summary = "카테고리별 동영상 조회", description = "특정 카테고리의 동영상을 조회합니다. 페이징 지원")
	@ApiResponse(responseCode = "200", description = "카테고리별 동영상 조회 성공")
	@GetMapping("/category/{categoryId}")
	public ResponseEntity<Map<String, Object>> getVideosByCategory(
			@Parameter(description = "카테고리 ID") @PathVariable int categoryId,
			@Parameter(description = "페이지 번호", example = "1") @RequestParam(required = false) Integer page,
			@Parameter(description = "페이지 크기", example = "10") @RequestParam(required = false) Integer size) {
		
		// 페이징 파라미터가 없으면 전체 조회
		if (page == null || size == null) {
			List<Video> videos = videoService.getVideosByCategory(categoryId);
			Map<String, Object> response = new HashMap<>();
			response.put("videos", videos);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		
		// 페이징 파라미터 검증
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;
		
		// offset 계산
		int offset = (page - 1) * size;
		
		// 페이징 처리
		List<Video> videos = videoService.getVideosByCategory(categoryId, offset, size);
		int totalCount = videoService.getVideosCountByCategory(categoryId);
		
		// 페이징 정보 포함한 응답
		Map<String, Object> response = new HashMap<>();
		response.put("videos", videos);
		response.put("currentPage", page);
		response.put("pageSize", size);
		response.put("totalCount", totalCount);
		response.put("totalPages", (int) Math.ceil((double) totalCount / size));
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	// 키워드로 비디오 검색 (페이징 지원)
	@Operation(summary = "동영상 검색", description = "키워드로 동영상을 검색합니다. 페이징 지원")
	@ApiResponse(responseCode = "200", description = "동영상 검색 성공")
	@GetMapping("/search")
	public ResponseEntity<Map<String, Object>> searchVideos(
			@Parameter(description = "검색 키워드", example = "투자") @RequestParam String keyword,
			@Parameter(description = "페이지 번호", example = "1") @RequestParam(required = false) Integer page,
			@Parameter(description = "페이지 크기", example = "10") @RequestParam(required = false) Integer size) {
		
		// 페이징 파라미터가 없으면 전체 조회
		if (page == null || size == null) {
			List<Video> videos = videoService.searchVideos(keyword);
			Map<String, Object> response = new HashMap<>();
			response.put("videos", videos);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		
		// 페이징 파라미터 검증
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;
		
		// offset 계산
		int offset = (page - 1) * size;
		
		// 페이징 처리
		List<Video> videos = videoService.searchVideos(keyword, offset, size);
		int totalCount = videoService.getSearchResultCount(keyword);
		
		// 페이징 정보 포함한 응답
		Map<String, Object> response = new HashMap<>();
		response.put("videos", videos);
		response.put("currentPage", page);
		response.put("pageSize", size);
		response.put("totalCount", totalCount);
		response.put("totalPages", (int) Math.ceil((double) totalCount / size));
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	// 찜 여부 확인
	@Operation(summary = "찜 상태 확인", description = "특정 동영상의 찜 상태를 확인합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "찜 상태 확인 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@GetMapping("/{videoId}/wish")
	public ResponseEntity<Boolean> checkWishStatus(@Parameter(description = "동영상 ID") @PathVariable int videoId, HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		boolean isWished = videoService.isVideoWished(userId, videoId);
		return new ResponseEntity<>(isWished, HttpStatus.OK);
	}
	
	// 찜 토글 (찜 추가/삭제)
	@Operation(summary = "찜 토글", description = "동영상을 찜하거나 찜을 해제합니다. 토글 방식으로 동작합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "찜 토글 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/{videoId}/wish")
	public ResponseEntity<Boolean> toggleWish(@Parameter(description = "동영상 ID") @PathVariable int videoId, HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		boolean isWished = videoService.isVideoWished(userId, videoId);
		
		if (isWished) {
			// 이미 찜한 상태면 찜 삭제
			videoService.removeVideoWish(userId, videoId);
			return new ResponseEntity<>(false, HttpStatus.OK);
		} else {
			// 찜하지 않은 상태면 찜 추가
			videoService.addVideoWish(userId, videoId);
			// 알림: 영상 업로더에게 찜 알림 (업로더가 본인이 아닐 때)
			try {
				Video video = videoService.getVideo(videoId);
				if (video != null && video.getUserId() != userId) {
					Notification n = new Notification();
					n.setRecipientId(video.getUserId());
					n.setActorId(userId);
					n.setType("WISH");
					n.setTargetType("VIDEO");
					n.setTargetId(videoId);
					try {
						String nickname = null;
						var actor = userService.getUserByUserId(userId);
						if (actor != null && actor.getNickname() != null && !actor.getNickname().isBlank()) {
							nickname = actor.getNickname();
						}
						if (nickname != null) {
							n.setMessage(nickname + "님이 당신의 영상을 찜했습니다.");
						} else {
							n.setMessage("사용자 " + userId + "님이 당신의 영상을 찜했습니다.");
						}
					} catch (Exception e) {
						n.setMessage("사용자 " + userId + "님이 당신의 영상을 찜했습니다.");
					}
					notificationService.createNotification(n);
				}
			} catch (Exception e) {
				// 알림 실패는 찜 동작에 영향 주지 않음
			}
			return new ResponseEntity<>(true, HttpStatus.OK);
		}
	}
	
	// 찜한 영상 목록 조회 (페이징 지원)
	@Operation(summary = "찜한 동영상 목록 조회", description = "사용자가 찜한 동영상 목록을 조회합니다. 페이징 지원")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "찜한 동영상 목록 조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@GetMapping("/wished")
	public ResponseEntity<Map<String, Object>> getWishedVideos(
			HttpServletRequest request,
			@Parameter(description = "페이지 번호", example = "1") @RequestParam(required = false) Integer page,
			@Parameter(description = "페이지 크기", example = "10") @RequestParam(required = false) Integer size) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		// 페이징 파라미터가 없으면 전체 조회
		if (page == null || size == null) {
			List<Video> wishedVideos = videoService.getWishedVideos(userId);
			Map<String, Object> response = new HashMap<>();
			response.put("videos", wishedVideos);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		
		// 페이징 파라미터 검증
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;
		
		// offset 계산
		int offset = (page - 1) * size;
		
		// 페이징 처리
		List<Video> wishedVideos = videoService.getWishedVideos(userId, offset, size);
		int totalCount = videoService.getWishedVideosCount(userId);
		
		// 페이징 정보 포함한 응답
		Map<String, Object> response = new HashMap<>();
		response.put("videos", wishedVideos);
		response.put("currentPage", page);
		response.put("pageSize", size);
		response.put("totalCount", totalCount);
		response.put("totalPages", (int) Math.ceil((double) totalCount / size));
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	// request attribute에서 userId 안전하게 추출
	private Integer getUserIdFromRequest(HttpServletRequest request) {
		Object attr = request.getAttribute("userId");
		if (attr instanceof Integer) {
			return (Integer) attr;
		}
		return null;
	}
}

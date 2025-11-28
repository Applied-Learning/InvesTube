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
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/videos")
public class VideoRestController {
	
	private final VideoService videoService;
	
	public VideoRestController(VideoService videoService) {
		this.videoService = videoService;
	}
	
	// 전체 비디오 조회 (정렬 기능 + 페이징)
	@GetMapping
	public ResponseEntity<Map<String, Object>> getAllVideos(
			@RequestParam(required = false, defaultValue = "latest") String sortBy,
			@RequestParam(required = false) Integer page,
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
	@GetMapping("/{videoId}")
	public ResponseEntity<Video> getVideo(@PathVariable int videoId) {
		Video video = videoService.getVideo(videoId);
		if (video != null) {
			// 조회수 증가
			videoService.increaseViewCount(videoId);
			return new ResponseEntity<>(video, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// 비디오 등록 (인증 필요)
	@PostMapping
	public ResponseEntity<Void> createVideo(@RequestBody Video video, HttpServletRequest request) {
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
	@PutMapping("/{videoId}")
	public ResponseEntity<Void> updateVideo(@PathVariable int videoId, @RequestBody Video video, HttpServletRequest request) {
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
	@DeleteMapping("/{videoId}")
	public ResponseEntity<Void> deleteVideo(@PathVariable int videoId, HttpServletRequest request) {
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
	@GetMapping("/category/{categoryId}")
	public ResponseEntity<Map<String, Object>> getVideosByCategory(
			@PathVariable int categoryId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		
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
	@GetMapping("/search")
	public ResponseEntity<Map<String, Object>> searchVideos(
			@RequestParam String keyword,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		
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
	@GetMapping("/{videoId}/wish")
	public ResponseEntity<Boolean> checkWishStatus(@PathVariable int videoId, HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		boolean isWished = videoService.isVideoWished(userId, videoId);
		return new ResponseEntity<>(isWished, HttpStatus.OK);
	}
	
	// 찜 토글 (찜 추가/삭제)
	@PostMapping("/{videoId}/wish")
	public ResponseEntity<Boolean> toggleWish(@PathVariable int videoId, HttpServletRequest request) {
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
			return new ResponseEntity<>(true, HttpStatus.OK);
		}
	}
	
	// 찜한 영상 목록 조회 (페이징 지원)
	@GetMapping("/wished")
	public ResponseEntity<Map<String, Object>> getWishedVideos(
			HttpServletRequest request,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
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

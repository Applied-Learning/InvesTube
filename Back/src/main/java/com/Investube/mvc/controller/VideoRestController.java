package com.Investube.mvc.controller;

import java.util.List;

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
	
	// 전체 비디오 조회
	@GetMapping
	public ResponseEntity<List<Video>> getAllVideos() {
		List<Video> videos = videoService.getAllVideos();
		return new ResponseEntity<>(videos, HttpStatus.OK);
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
	
	// 카테고리별 비디오 조회
	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<Video>> getVideosByCategory(@PathVariable int categoryId) {
		List<Video> videos = videoService.getVideosByCategory(categoryId);
		return new ResponseEntity<>(videos, HttpStatus.OK);
	}
	
	// 키워드로 비디오 검색
	@GetMapping("/search")
	public ResponseEntity<List<Video>> searchVideos(@RequestParam String keyword) {
		List<Video> videos = videoService.searchVideos(keyword);
		return new ResponseEntity<>(videos, HttpStatus.OK);
	}
	
	// 유틸: request attribute에서 userId 안전하게 추출
	private Integer getUserIdFromRequest(HttpServletRequest request) {
		Object attr = request.getAttribute("userId");
		if (attr instanceof Integer) {
			return (Integer) attr;
		}
		return null;
	}
}

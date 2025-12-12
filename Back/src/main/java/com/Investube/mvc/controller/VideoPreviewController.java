package com.Investube.mvc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Investube.mvc.model.dto.Video;
import com.Investube.mvc.model.service.VideoService;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * 동영상 기본 정보(조회수 증가 없이) 조회용 컨트롤러.
 * 마이페이지 등 프리뷰 영역에서 사용한다.
 */
@RestController
@RequestMapping("/videos")
public class VideoPreviewController {

	private final VideoService videoService;

	public VideoPreviewController(VideoService videoService) {
		this.videoService = videoService;
	}

	// 비디오 상세 프리뷰 조회 (조회수 증가 없음)
	@GetMapping("/{videoId}/preview")
	public ResponseEntity<Video> getVideoPreview(
			@Parameter(description = "영상 ID") @PathVariable int videoId) {
		Video video = videoService.getVideo(videoId);
		if (video != null) {
			return new ResponseEntity<>(video, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}


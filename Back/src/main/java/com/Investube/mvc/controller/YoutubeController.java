package com.Investube.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Investube.mvc.model.dto.YoutubeVideoResponse;
import com.Investube.mvc.model.service.YoutubeService;

@RestController
@RequestMapping("/youtube")
public class YoutubeController {

	private final YoutubeService youtubeService;

	public YoutubeController(YoutubeService youtubeService) {
		this.youtubeService = youtubeService;
	}

	@GetMapping("/search")
	public ResponseEntity<Map<String, Object>> searchVideos(
			@RequestParam(name = "query", required = true) String query,
			@RequestParam(name = "maxResults", required = false, defaultValue = "10") int maxResults) {
		List<YoutubeVideoResponse> items = youtubeService.searchVideos(query, maxResults);
		Map<String, Object> body = new HashMap<>();
		body.put("items", items);
		return new ResponseEntity<>(body, HttpStatus.OK);
	}

	@GetMapping("/videos/{id}")
	public ResponseEntity<YoutubeVideoResponse> getVideo(@PathVariable("id") String id) {
		YoutubeVideoResponse item = youtubeService.getVideo(id);
		if (item == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(item, HttpStatus.OK);
	}
}

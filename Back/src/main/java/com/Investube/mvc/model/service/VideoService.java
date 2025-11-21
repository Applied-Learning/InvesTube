package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.Video;

public interface VideoService {
	
	// 전체 비디오 조회
	List<Video> getAllVideos();
	
	// 비디오 ID로 조회
	Video getVideo(int videoId);
	
	// 비디오 등록
	boolean createVideo(Video video);
	
	// 비디오 수정
	boolean modifyVideo(Video video);
	
	// 비디오 삭제
	boolean removeVideo(int videoId);
	
	// 운동 부위별 비디오 조회
	List<Video> getVideosByPart(String part);
	
	// 카테고리별 비디오 조회
	List<Video> getVideosByCategory(String category);
	
	// 키워드로 비디오 검색
	List<Video> searchVideos(String keyword);
	
	// 조회수 증가
	boolean increaseViewCount(int videoId);
}

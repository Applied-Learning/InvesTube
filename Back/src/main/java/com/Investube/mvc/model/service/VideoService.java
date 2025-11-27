package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.Video;

public interface VideoService {
	
	// 전체 비디오 조회
	List<Video> getAllVideos();
	
	// 조회수순 비디오 조회
	List<Video> getVideosByViews();
	
	// 평점순 비디오 조회
	List<Video> getVideosByRating();
	
	// 비디오 ID로 조회
	Video getVideo(int videoId);
	
	// 비디오 등록
	boolean createVideo(Video video);
	
	// 비디오 수정
	boolean modifyVideo(Video video);
	
	// 비디오 삭제
	boolean removeVideo(int videoId);
	
	// 카테고리별 비디오 조회
	List<Video> getVideosByCategory(int categoryId);
	
	// 키워드로 비디오 검색
	List<Video> searchVideos(String keyword);
	
	// 조회수 증가
	boolean increaseViewCount(int videoId);
	
	// 찜 여부 확인
	boolean isVideoWished(int userId, int videoId);
	
	// 찜 추가
	boolean addVideoWish(int userId, int videoId);
	
	// 찜 삭제
	boolean removeVideoWish(int userId, int videoId);
	
	// 찜한 영상 목록 조회
	List<Video> getWishedVideos(int userId);
}

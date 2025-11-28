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
	
	// 페이징 - 전체 비디오 조회
	List<Video> getAllVideos(int offset, int size);
	
	// 페이징 - 조회수순 비디오 조회
	List<Video> getVideosByViews(int offset, int size);
	
	// 페이징 - 평점순 비디오 조회
	List<Video> getVideosByRating(int offset, int size);
	
	// 전체 비디오 개수 조회
	int getTotalVideoCount();
	
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
	
	// 페이징 - 카테고리별 비디오 조회
	List<Video> getVideosByCategory(int categoryId, int offset, int size);
	
	// 카테고리별 비디오 개수 조회
	int getVideosCountByCategory(int categoryId);
	
	// 키워드로 비디오 검색
	List<Video> searchVideos(String keyword);
	
	// 페이징 - 키워드로 비디오 검색
	List<Video> searchVideos(String keyword, int offset, int size);
	
	// 검색 결과 개수 조회
	int getSearchResultCount(String keyword);
	
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
	
	// 페이징 - 찜한 영상 목록 조회
	List<Video> getWishedVideos(int userId, int offset, int size);
	
	// 찜한 영상 개수 조회
	int getWishedVideosCount(int userId);
	
	// 업로드한 영상 조회
	List<Video> getVideosByUser(int userId);
}

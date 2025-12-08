package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.Investube.mvc.model.dto.Video;
import com.Investube.mvc.model.dto.VideoWish;

@Mapper
public interface VideoDao {
	
	// 전체 비디오 조회
	List<Video> selectAll();
	
	// 조회수순 비디오 조회
	List<Video> selectByViews();
	
	// 평점순 비디오 조회
	List<Video> selectByRating();
	
	// 페이징 - 전체 비디오 조회
	List<Video> selectAllWithPaging(@Param("offset") int offset, @Param("size") int size);
	
	// 페이징 - 조회수순 비디오 조회
	List<Video> selectByViewsWithPaging(@Param("offset") int offset, @Param("size") int size);
	
	// 페이징 - 평점순 비디오 조회
	List<Video> selectByRatingWithPaging(@Param("offset") int offset, @Param("size") int size);
	
	// 전체 비디오 개수 조회
	int selectTotalCount();
	
	// 비디오 ID로 조회
	Video selectOne(int videoId);
	
	// 비디오 등록
	int insertVideo(Video video);
	
	// 비디오 수정
	int updateVideo(Video video);
	
	// 비디오 삭제
	int deleteVideo(int videoId);
	
	// 카테고리별 비디오 조회
	List<Video> selectByCategory(int categoryId);
	
	// 페이징 - 카테고리별 비디오 조회
	List<Video> selectByCategoryWithPaging(@Param("categoryId") int categoryId, @Param("offset") int offset, @Param("size") int size);
	
	// 카테고리별 비디오 개수 조회
	int selectCountByCategory(int categoryId);
	
	// 키워드로 비디오 검색
	List<Video> searchByKeyword(String keyword);
	
	// 페이징 - 키워드로 비디오 검색
	List<Video> searchByKeywordWithPaging(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
	
	// 검색 결과 개수 조회
	int selectCountByKeyword(String keyword);
	
	// 조회수 증가
	int updateViewCount(int videoId);
	
	// 찜 여부 조회
	VideoWish selectWish(@Param("userId") int userId, @Param("videoId") int videoId);
	
	// 찜 추가
	int insertWish(@Param("userId") int userId, @Param("videoId") int videoId);
	
	// 찜 삭제
	int deleteWish(@Param("userId") int userId, @Param("videoId") int videoId);
	
	// 찜한 영상 목록 조회
	List<Video> selectWishedVideos(int userId);
	
	// 페이징 - 찜한 영상 목록 조회
	List<Video> selectWishedVideosWithPaging(@Param("userId") int userId, @Param("offset") int offset, @Param("size") int size);
	
	// 찜한 영상 개수 조회
	int selectCountWishedVideos(int userId);
	
	// 업로드한 영상 조회
	List<Video> getVideosByUser(@Param("userId") int userId);
	
	// 찜 수 증가
	int incrementWishCount(int videoId);
	
	// 찜 수 감소
	int decrementWishCount(int videoId);
}

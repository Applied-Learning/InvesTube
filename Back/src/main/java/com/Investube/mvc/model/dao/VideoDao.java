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
	
	// 키워드로 비디오 검색
	List<Video> searchByKeyword(String keyword);
	
	// 조회수 증가
	int updateViewCount(int videoId);
	
	// 찜 여부 조회
	VideoWish selectWish(@Param("userId") int userId, @Param("videoId") int videoId);
	
	// 찜 추가
	int insertWish(@Param("userId") int userId, @Param("videoId") int videoId);
	
	// 찜 삭제
	int deleteWish(@Param("userId") int userId, @Param("videoId") int videoId);
}

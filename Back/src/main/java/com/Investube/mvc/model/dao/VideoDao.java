package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.Investube.mvc.model.dto.Video;

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
	
	// 운동 부위별 비디오 조회
	List<Video> selectByPart(String part);
	
	// 카테고리별 비디오 조회
	List<Video> selectByCategory(String category);
	
	// 키워드로 비디오 검색
	List<Video> searchByKeyword(String keyword);
	
	// 조회수 증가
	int updateViewCount(int videoId);
}

package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.Investube.mvc.model.dto.BoardImage;
import com.Investube.mvc.model.dto.BoardPost;

@Mapper
public interface BoardDao {
	
	// 게시글 목록 조회 (페이징)
	List<BoardPost> getBoardList(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
	
	// 게시글 총 개수
	int getTotalCount(@Param("keyword") String keyword);
	
	// 게시글 상세 조회
    BoardPost getPostById(int postId);
    
    // 게시글 작성
    int createPost(BoardPost post);
    
    // 게시글 수정
    int updatePost(BoardPost post);
    
    // 게시글 삭제
    int deletePost(int postId);
    
    // 사용자별 게시글 조회
    List<BoardPost> getPostsByUserId(@Param("userId") int userId, @Param("offset") int offset, @Param("size") int size);
    
    // 사용자별 게시글 개수
    int getPostCountByUserId(int userId);
    
    // 이미지
    int insertImages(List<BoardImage> images);
    
    List<BoardImage> getImagesByPostId(int postId);
}

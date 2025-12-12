package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.BoardImage;
import com.Investube.mvc.model.dto.BoardPost;

public interface BoardService {
	
	// 게시글 목록 조회 (페이징, 검색, 정렬)
	List<BoardPost> getBoardList(String keyword, String sortBy, int offset, int size);
	
	// 게시글 총 개수
	int getTotalCount(String keyword);
	
	// 게시글 상세 조회
    BoardPost getPostById(int postId);

    // 게시글 조회수 증가
    boolean increaseViewCount(int postId);
    
    // 게시글 작성
    int createPost(BoardPost post);
    
    // 게시글 수정
    int updatePost(BoardPost post);
    
    // 게시글 삭제
    int deletePost(int postId);
    
    // 사용자별 게시글 조회
    List<BoardPost> getPostsByUserId(int userId, int offset, int size);
    
    // 사용자별 게시글 개수
    int getPostCountByUserId(int userId);

	// 이미지 저장
    int insertImages(List<BoardImage> images);
    
    // 게시글의 이미지 목록 조회 (파일 삭제용)
    List<BoardImage> getImagesByPostId(int postId);

    // 단일 이미지 조회
    BoardImage getImageById(int imageId);

    // 단일 이미지 삭제
    int deleteImage(int imageId);
}
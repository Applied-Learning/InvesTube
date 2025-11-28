package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.Investube.mvc.model.dto.BoardImage;
import com.Investube.mvc.model.dto.BoardPost;

@Mapper
public interface BoardDao {
	
	// 게시글
	List<BoardPost> getBoardList(String keyword);
	
    BoardPost getPostById(int postId);
    
    int createPost(BoardPost post);
    
    int updatePost(BoardPost post);
    
    int deletePost(int postId);
    
    // 이미지
    int insertImages(List<BoardImage> images);
    
    List<BoardImage> getImagesByPostId(int postId);
}

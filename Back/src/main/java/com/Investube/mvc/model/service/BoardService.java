package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.BoardImage;
import com.Investube.mvc.model.dto.BoardPost;

public interface BoardService {
	
	List<BoardPost> getBoardList(String keyword);
	
    BoardPost getPostById(int postId);
    
    int createPost(BoardPost post);
    
    int updatePost(BoardPost post);
    
    int deletePost(int postId);

    int insertImages(List<BoardImage> images);
    
    List<BoardImage> getImagesByPostId(int postId);
}

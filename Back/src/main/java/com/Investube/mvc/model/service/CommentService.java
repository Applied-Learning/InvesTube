package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.BoardComment;

public interface CommentService {
	
	List<BoardComment> getCommentsByPostId(int postId);
	
    int insertComment(BoardComment comment);
    
    int updateComment(BoardComment comment);
    
    int deleteComment(int commentId);
}

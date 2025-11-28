package com.Investube.mvc.model.dao;

import java.util.List;

import com.Investube.mvc.model.dto.BoardComment;

public interface CommentDao {

	List<BoardComment> getCommentsByPostId(int postId);
	
    int insertComment(BoardComment comment);
    
    int updateComment(BoardComment comment);
    
    int deleteComment(int commentId);
}

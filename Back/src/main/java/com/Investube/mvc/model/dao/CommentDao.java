package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.Investube.mvc.model.dto.BoardComment;

public interface CommentDao {

	List<BoardComment> getCommentsByPostId(int postId);
	
    int insertComment(BoardComment comment);
    
    int updateComment(BoardComment comment);
    
    int deleteComment(int commentId);

    List<BoardComment> getCommentedPostsByUser(@Param("userId") int userId,
                                               @Param("limit") int limit);
}

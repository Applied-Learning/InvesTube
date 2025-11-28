package com.Investube.mvc.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dao.CommentDao;
import com.Investube.mvc.model.dto.BoardComment;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;

    public CommentServiceImpl(CommentDao commentDao) {
        this.commentDao = commentDao;
    }

    @Override
    public List<BoardComment> getCommentsByPostId(int postId) {
        return commentDao.getCommentsByPostId(postId);
    }

    @Override
    public int insertComment(BoardComment comment) {
        return commentDao.insertComment(comment);
    }

    @Override
    public int updateComment(BoardComment comment) {
        return commentDao.updateComment(comment);
    }

    @Override
    public int deleteComment(int commentId) {
        return commentDao.deleteComment(commentId);
    }
}

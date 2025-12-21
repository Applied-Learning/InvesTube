package com.Investube.mvc.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dao.BoardDao;
import com.Investube.mvc.model.dto.BoardPost;

@Service
public class BoardServiceImpl implements BoardService {

	private final BoardDao boardDao;

    public BoardServiceImpl(BoardDao boardDao) {
        this.boardDao = boardDao;
    }
    
	@Override
	public List<BoardPost> getBoardList(String keyword, String sortBy, int offset, int size) {
		return boardDao.getBoardList(keyword, sortBy, offset, size);
	}
	
	@Override
	public int getTotalCount(String keyword) {
		return boardDao.getTotalCount(keyword);
	}

	@Override
	public BoardPost getPostById(int postId) {
		return boardDao.getPostById(postId);
	}

	@Override
	public boolean increaseViewCount(int postId) {
		return boardDao.updateViewCount(postId) > 0;
	}
	
	@Override
	public List<BoardPost> getPostsByUserId(int userId, int offset, int size) {
		return boardDao.getPostsByUserId(userId, offset, size);
	}
	
	@Override
	public int getPostCountByUserId(int userId) {
		return boardDao.getPostCountByUserId(userId);
	}

	@Override
	public int createPost(BoardPost post) {
		return boardDao.createPost(post);
	}

	@Override
	public int updatePost(BoardPost post) {
		return boardDao.updatePost(post);
	}

	@Override
	public int deletePost(int postId) {
		// 현재는 게시글만 삭제 (이미지는 content 내 URL로만 관리)
		return boardDao.deletePost(postId);
	}
}


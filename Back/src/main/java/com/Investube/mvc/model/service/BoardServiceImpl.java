package com.Investube.mvc.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dao.BoardDao;
import com.Investube.mvc.model.dto.BoardImage;
import com.Investube.mvc.model.dto.BoardPost;

@Service
public class BoardServiceImpl implements BoardService {

	private final BoardDao boardDao;

    public BoardServiceImpl(BoardDao boardDao) {
        this.boardDao = boardDao;
    }
    
	@Override
	public List<BoardPost> getBoardList(String keyword) {
		return boardDao.getBoardList(keyword);
	}

	@Override
	public BoardPost getPostById(int postId) {
		return boardDao.getPostById(postId);
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
		return boardDao.deletePost(postId);
	}

	@Override
	public int insertImages(List<BoardImage> images) {
		return boardDao.insertImages(images);
	}

	@Override
	public List<BoardImage> getImagesByPostId(int postId) {
		return boardDao.getImagesByPostId(postId);
	}

}

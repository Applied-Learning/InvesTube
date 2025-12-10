package com.Investube.mvc.model.service;

import java.io.File;
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
	public List<BoardPost> getBoardList(String keyword, int offset, int size) {
		return boardDao.getBoardList(keyword, offset, size);
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
		// 1. DB에서 이미지 목록 조회
		List<BoardImage> images = boardDao.getImagesByPostId(postId);
		
		// 2. 파일 시스템에서 실제 파일 삭제
		if (images != null) {
			for (BoardImage img : images) {
				String url = img.getImageUrl(); // 예: /uploads/board/12345_test.jpg
				if (url != null && url.startsWith("/")) {
					// 애플리케이션 기준 상대 경로로 가정
					File file = new File("." + url); // ./uploads/board/...
					if (file.exists()) {
						file.delete();
					}
				}
			}
		}
		
		// 3. 게시글 삭제 (DB에서는 CASCADE로 이미지 레코드도 함께 삭제됨)
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
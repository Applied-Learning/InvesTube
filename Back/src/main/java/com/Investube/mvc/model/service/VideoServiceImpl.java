package com.Investube.mvc.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dao.VideoDao;
import com.Investube.mvc.model.dto.Video;

@Service
public class VideoServiceImpl implements VideoService {
	
	private final VideoDao videoDao;
	
	public VideoServiceImpl(VideoDao videoDao) {
		this.videoDao = videoDao;
	}

	@Override
	public List<Video> getAllVideos() {
		return videoDao.selectAll();
	}

	@Override
	public List<Video> getVideosByViews() {
		return videoDao.selectByViews();
	}

	@Override
	public List<Video> getVideosByRating() {
		return videoDao.selectByRating();
	}

	@Override
	public Video getVideo(int videoId) {
		return videoDao.selectOne(videoId);
	}

	@Override
	public boolean createVideo(Video video) {
		return videoDao.insertVideo(video) > 0;
	}

	@Override
	public boolean modifyVideo(Video video) {
		return videoDao.updateVideo(video) > 0;
	}

	@Override
	public boolean removeVideo(int videoId) {
		return videoDao.deleteVideo(videoId) > 0;
	}

	@Override
	public List<Video> getVideosByCategory(int categoryId) {
		return videoDao.selectByCategory(categoryId);
	}

	@Override
	public List<Video> searchVideos(String keyword) {
		return videoDao.searchByKeyword(keyword);
	}

	@Override
	public boolean increaseViewCount(int videoId) {
		return videoDao.updateViewCount(videoId) > 0;
	}

	@Override
	public boolean isVideoWished(int userId, int videoId) {
		return videoDao.selectWish(userId, videoId) != null;
	}

	@Override
	public boolean addVideoWish(int userId, int videoId) {
		return videoDao.insertWish(userId, videoId) > 0;
	}

	@Override
	public boolean removeVideoWish(int userId, int videoId) {
		return videoDao.deleteWish(userId, videoId) > 0;
	}

	@Override
	public List<Video> getWishedVideos(int userId) {
		return videoDao.selectWishedVideos(userId);
	}
}

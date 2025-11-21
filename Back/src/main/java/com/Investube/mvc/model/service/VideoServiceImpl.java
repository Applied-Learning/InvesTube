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
	public List<Video> getVideosByPart(String part) {
		return videoDao.selectByPart(part);
	}

	@Override
	public List<Video> getVideosByCategory(String category) {
		return videoDao.selectByCategory(category);
	}

	@Override
	public List<Video> searchVideos(String keyword) {
		return videoDao.searchByKeyword(keyword);
	}

	@Override
	public boolean increaseViewCount(int videoId) {
		return videoDao.updateViewCount(videoId) > 0;
	}
}

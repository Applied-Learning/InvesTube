package com.Investube.mvc.model.service;

import com.Investube.mvc.model.dao.StockWishDao;
import com.Investube.mvc.model.dto.StockWish;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockWishServiceImpl implements StockWishService {

    private final StockWishDao stockWishDao;

    public StockWishServiceImpl(StockWishDao stockWishDao) {
        this.stockWishDao = stockWishDao;
    }

    @Override
    public List<StockWish> getWishedStocksByUserId(int userId) {
        return stockWishDao.getWishedStocksByUserId(userId);
    }

    @Override
    public boolean isWished(int userId, String stockCode) {
        return stockWishDao.isWished(userId, stockCode);
    }

    @Override
    public boolean addWish(int userId, String stockCode) {
        return stockWishDao.addWish(userId, stockCode) > 0;
    }

    @Override
    public boolean removeWish(int userId, String stockCode) {
        return stockWishDao.removeWish(userId, stockCode) > 0;
    }

    @Override
    public int getWishCount(String stockCode) {
        return stockWishDao.getWishCount(stockCode);
    }
}

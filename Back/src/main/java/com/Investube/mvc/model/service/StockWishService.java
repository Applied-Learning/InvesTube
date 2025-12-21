package com.Investube.mvc.model.service;

import com.Investube.mvc.model.dto.StockWish;

import java.util.List;

public interface StockWishService {
    
    List<StockWish> getWishedStocksByUserId(int userId);
    
    boolean isWished(int userId, String stockCode);
    
    boolean addWish(int userId, String stockCode);
    
    boolean removeWish(int userId, String stockCode);
    
    int getWishCount(String stockCode);
}

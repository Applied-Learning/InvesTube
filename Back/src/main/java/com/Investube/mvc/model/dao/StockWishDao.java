package com.Investube.mvc.model.dao;

import com.Investube.mvc.model.dto.StockWish;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StockWishDao {
    
    List<StockWish> getWishedStocksByUserId(@Param("userId") int userId);
    
    boolean isWished(@Param("userId") int userId, @Param("stockCode") String stockCode);
    
    int addWish(@Param("userId") int userId, @Param("stockCode") String stockCode);
    
    int removeWish(@Param("userId") int userId, @Param("stockCode") String stockCode);
    
    int getWishCount(@Param("stockCode") String stockCode);
}

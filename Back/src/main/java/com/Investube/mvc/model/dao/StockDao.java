package com.Investube.mvc.model.dao;

import java.util.List;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.dto.StockPrice;
import com.Investube.mvc.model.dto.StockDetailDto;

public interface StockDao {
    
    // Stock 관련
    List<Stock> selectAllStocks();
    Stock selectStockByCode(String stockCode);
    int insertStock(Stock stock);
    int updateStock(Stock stock);
    int deleteStock(String stockCode);
    
    // StockPrice 관련
    List<StockPrice> selectPricesByStockCode(String stockCode);
    List<StockPrice> selectPricesByStockCodeAndDateRange(String stockCode, String startDate, String endDate);
    StockPrice selectLatestPriceByStockCode(String stockCode);
    int insertStockPrice(StockPrice stockPrice);
    int updateStockPrice(StockPrice stockPrice);
    
    // 상세 정보
    List<StockDetailDto> selectStockDetailsWithLatestPrice();
    StockDetailDto selectStockDetailByCode(String stockCode);
}

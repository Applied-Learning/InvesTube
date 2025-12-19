package com.Investube.mvc.model.service;

import java.util.List;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.dto.StockPrice;
import com.Investube.mvc.model.dto.StockDetailDto;

public interface StockService {
    
    // Stock 관련
    List<Stock> getAllStocks();
    Stock getStockByCode(String stockCode);
    boolean registerStock(Stock stock);
    boolean modifyStock(Stock stock);
    boolean removeStock(String stockCode);
    
    // StockPrice 관련
    List<StockPrice> getPricesByStockCode(String stockCode);
    List<StockPrice> getPricesByStockCodeAndDateRange(String stockCode, String startDate, String endDate);
    StockPrice getLatestPrice(String stockCode);
    boolean registerStockPrice(StockPrice stockPrice);
    
    // 상세 정보
    List<StockDetailDto> getStockListWithLatestPrice();
    StockDetailDto getStockDetail(String stockCode);
    
    // API 연동
    void syncStockDataFromDart();
    void syncStockPriceFromKrx(String stockCode);
}

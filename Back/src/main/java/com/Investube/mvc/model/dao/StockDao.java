package com.Investube.mvc.model.dao;

import java.util.List;

import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.dto.StockDetailDto;
import com.Investube.mvc.model.dto.StockPrice;

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

    // 리스트 정보
    List<StockDetailDto> selectStockDetailsWithLatestPrice();

    StockDetailDto selectStockDetailByCode(String stockCode);

    // 종목명으로검색(LIKE 검색)
    Stock selectStockByName(String stockName);

    // 총종목 수
    int countStocks();

    // 다건 주가 배치 업서트
    int insertStockPricesBatch(List<StockPrice> prices);

    // market이 null인 종목 수
    int countStocksWithNullMarket();
}

package com.Investube.mvc.model.service;

import java.util.List;
import java.util.Map;

import com.Investube.mvc.model.dto.KrxIndexResponse;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.dto.StockDetailDto;
import com.Investube.mvc.model.dto.StockPrice;

public interface StockService {

    // Stock 관련
    List<Stock> getAllStocks();

    Stock getStockByCode(String stockCode);

    Stock getStockByName(String stockName);

    boolean registerStock(Stock stock);

    boolean modifyStock(Stock stock);

    boolean removeStock(String stockCode);

    // StockPrice 관련
    List<StockPrice> getPricesByStockCode(String stockCode);

    List<StockPrice> getPricesByStockCodeAndDateRange(String stockCode, String startDate, String endDate);

    StockPrice getLatestPrice(String stockCode);

    boolean registerStockPrice(StockPrice stockPrice);

    // 리스트 정보
    List<StockDetailDto> getStockListWithLatestPrice();

    StockDetailDto getStockDetail(String stockCode);

    // API 연동
    void syncStockDataFromDart(); // 초기 전체 동기화 (90일)

    void syncTodayStockData(); // 오늘 데이터만 동기화 (스케줄러용)

    void syncStockDataByDate(String dateStr); // 특정 날짜 동기화 (yyyyMMdd 포맷)

    void syncStockPriceFromKrx(String stockCode);

    // 지수정보 조회
    List<KrxIndexResponse.KrxIndexItem> getKrxIndices();

    // DB 데이터 -> JSON 파일로 백업/캐시
    void exportDbToJson();

    // JSON 파일 -> DB 초기 적재/복원
    Map<String, Integer> importStockPricesFromJson();

    // 누락된 market 정보 업데이트
    void updateMissingStockInfo();

    // CSV(종목코드→업종)로 industry 일괄 업데이트
    int updateIndustryFromCsv(String csvPath);
}

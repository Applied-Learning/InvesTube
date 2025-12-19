package com.Investube.mvc.model.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.Investube.mvc.model.dao.StockDao;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.dto.StockPrice;
import com.Investube.mvc.model.dto.StockDetailDto;
import com.Investube.mvc.model.dto.KrxStockResponse;

@Service
public class StockServiceImpl implements StockService {
    
    @Autowired
    private StockDao stockDao;
    
    @Value("${krx.api.key}")
    private String krxApiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public List<Stock> getAllStocks() {
        return stockDao.selectAllStocks();
    }
    
    @Override
    public Stock getStockByCode(String stockCode) {
        return stockDao.selectStockByCode(stockCode);
    }
    
    @Override
    @Transactional
    public boolean registerStock(Stock stock) {
        return stockDao.insertStock(stock) == 1;
    }
    
    @Override
    @Transactional
    public boolean modifyStock(Stock stock) {
        return stockDao.updateStock(stock) == 1;
    }
    
    @Override
    @Transactional
    public boolean removeStock(String stockCode) {
        return stockDao.deleteStock(stockCode) == 1;
    }
    
    @Override
    public List<StockPrice> getPricesByStockCode(String stockCode) {
        return stockDao.selectPricesByStockCode(stockCode);
    }
    
    @Override
    public List<StockPrice> getPricesByStockCodeAndDateRange(String stockCode, String startDate, String endDate) {
        return stockDao.selectPricesByStockCodeAndDateRange(stockCode, startDate, endDate);
    }
    
    @Override
    public StockPrice getLatestPrice(String stockCode) {
        return stockDao.selectLatestPriceByStockCode(stockCode);
    }
    
    @Override
    @Transactional
    public boolean registerStockPrice(StockPrice stockPrice) {
        return stockDao.insertStockPrice(stockPrice) == 1;
    }
    
    @Override
    public List<StockDetailDto> getStockListWithLatestPrice() {
        return stockDao.selectStockDetailsWithLatestPrice();
    }
    
    @Override
    public StockDetailDto getStockDetail(String stockCode) {
        return stockDao.selectStockDetailByCode(stockCode);
    }
    
    @Override
    @Transactional
    public void syncStockDataFromDart() {
        // KOSPI 시장 데이터 가져오기
        syncKospiStocks();
        // KOSDAQ 시장 데이터 가져오기
        syncKosdaqStocks();
    }
    
    @Override
    @Transactional
    public void syncStockPriceFromKrx(String stockCode) {
        // 특정 종목 동기화는 현재 미구현 (전체 동기화만 지원)
        System.out.println("개별 종목 동기화는 전체 동기화를 사용하세요: syncStockDataFromDart()");
    }
    
    /**
     * KRX KOSPI 시장 주식 정보를 가져와서 DB에 저장
     */
    @Transactional
    public void syncKospiStocks() {
        try {
            // 한 달 전 날짜를 기준일자로 사용
            String baseDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // KRX API URL (유가증권 일별매매정보)
            String apiUrl = "https://data-dbg.krx.co.kr/svc/apis/sto/stk_bydd_trd";
            
            // 요청 파라미터
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> inBlock = new HashMap<>();
            inBlock.put("basDd", baseDate);
            requestBody.put("InBlock_1", inBlock);
            
            // HTTP 헤더에 API 키 추가
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("AUTH_KEY", krxApiKey);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            // API 호출
            System.out.println("KRX KOSPI API 호출 날짜: " + baseDate);
            
            // 실제 객체로 파싱
            org.springframework.http.ResponseEntity<KrxStockResponse> responseEntity = 
                restTemplate.exchange(apiUrl, org.springframework.http.HttpMethod.POST, requestEntity, KrxStockResponse.class);
            
            KrxStockResponse response = responseEntity.getBody();
            
            if (response != null && response.getOutBlock_1() != null) {
                List<KrxStockResponse.KrxStockItem> items = response.getOutBlock_1();
                
                System.out.println("KOSPI에서 " + items.size() + "개 종목 정보 수신");
                
                for (KrxStockResponse.KrxStockItem item : items) {
                    try {
                        // Stock 정보 저장/업데이트
                        Stock stock = new Stock();
                        stock.setStockCode(item.getISU_CD());
                        stock.setStockName(item.getISU_NM());
                        stock.setMarket("KOSPI");
                        stock.setIndustry(item.getSECT_TP_NM());
                        
                        // 기존에 있는지 확인
                        Stock existingStock = stockDao.selectStockByCode(item.getISU_CD());
                        if (existingStock == null) {
                            stockDao.insertStock(stock);
                        } else {
                            stockDao.updateStock(stock);
                        }
                        
                        // StockPrice 정보 저장
                        StockPrice stockPrice = new StockPrice();
                        stockPrice.setStockCode(item.getISU_CD());
                        stockPrice.setTradeDate(parseDate(item.getBAS_DD()));
                        stockPrice.setOpenPrice(parseBigDecimal(item.getTDD_OPNPRC()));
                        stockPrice.setHighPrice(parseBigDecimal(item.getTDD_HGPRC()));
                        stockPrice.setLowPrice(parseBigDecimal(item.getTDD_LWPRC()));
                        stockPrice.setClosePrice(parseBigDecimal(item.getTDD_CLSPRC()));
                        stockPrice.setVolume(parseLong(item.getACC_TRDVOL()));
                        stockPrice.setMarketCap(parseLong(item.getMKTCAP()));
                        
                        // INSERT ON DUPLICATE KEY UPDATE 방식으로 저장
                        stockDao.insertStockPrice(stockPrice);
                        
                    } catch (Exception e) {
                        System.err.println("KOSPI 종목 " + item.getISU_NM() + " 저장 실패: " + e.getMessage());
                    }
                }
                
                System.out.println("KOSPI 데이터 동기화 완료");
            } else {
                System.out.println("KOSPI API 응답 데이터가 없습니다.");
            }
            
        } catch (Exception e) {
            System.err.println("KOSPI API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("KOSPI 데이터 동기화 실패", e);
        }
    }
    
    /**
     * KRX KOSDAQ 시장 주식 정보를 가져와서 DB에 저장
     */
    @Transactional
    public void syncKosdaqStocks() {
        try {
            // 한 달 전 날짜를 기준일자로 사용
            String baseDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // KRX API URL
            String apiUrl = "https://data-dbg.krx.co.kr/svc/apis/sto/ksq_bydd_trd";
            
            // 요청 파라미터
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> inBlock = new HashMap<>();
            inBlock.put("basDd", baseDate);
            requestBody.put("InBlock_1", inBlock);
            
            // HTTP 헤더에 API 키 추가
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("AUTH_KEY", krxApiKey);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            // API 호출
            System.out.println("KRX API 호출 날짜: " + baseDate);
            System.out.println("요청 URL: " + apiUrl);
            System.out.println("요청 본문: " + requestBody);
            
            // 먼저 String으로 응답 확인
            org.springframework.http.ResponseEntity<String> stringResponse = 
                restTemplate.exchange(apiUrl, org.springframework.http.HttpMethod.POST, requestEntity, String.class);
            
            System.out.println("응답 상태 코드: " + stringResponse.getStatusCode());
            System.out.println("응답 본문 (원본): " + stringResponse.getBody());
            
            // 실제 객체로 파싱
            org.springframework.http.ResponseEntity<KrxStockResponse> responseEntity = 
                restTemplate.exchange(apiUrl, org.springframework.http.HttpMethod.POST, requestEntity, KrxStockResponse.class);
            
            KrxStockResponse response = responseEntity.getBody();
            
            if (response != null && response.getOutBlock_1() != null) {
                List<KrxStockResponse.KrxStockItem> items = response.getOutBlock_1();
                
                System.out.println("KRX에서 " + items.size() + "개 종목 정보 수신");
                
                for (KrxStockResponse.KrxStockItem item : items) {
                    try {
                        // Stock 정보 저장/업데이트
                        Stock stock = new Stock();
                        stock.setStockCode(item.getISU_CD());
                        stock.setStockName(item.getISU_NM());
                        stock.setMarket("KOSDAQ");
                        stock.setIndustry(item.getSECT_TP_NM());
                        
                        // 기존에 있는지 확인
                        Stock existingStock = stockDao.selectStockByCode(item.getISU_CD());
                        if (existingStock == null) {
                            stockDao.insertStock(stock);
                        } else {
                            stockDao.updateStock(stock);
                        }
                        
                        // StockPrice 정보 저장
                        StockPrice stockPrice = new StockPrice();
                        stockPrice.setStockCode(item.getISU_CD());
                        stockPrice.setTradeDate(parseDate(item.getBAS_DD()));
                        stockPrice.setOpenPrice(parseBigDecimal(item.getTDD_OPNPRC()));
                        stockPrice.setHighPrice(parseBigDecimal(item.getTDD_HGPRC()));
                        stockPrice.setLowPrice(parseBigDecimal(item.getTDD_LWPRC()));
                        stockPrice.setClosePrice(parseBigDecimal(item.getTDD_CLSPRC()));
                        stockPrice.setVolume(parseLong(item.getACC_TRDVOL()));
                        stockPrice.setMarketCap(parseLong(item.getMKTCAP()));
                        
                        // INSERT ON DUPLICATE KEY UPDATE 방식으로 저장
                        stockDao.insertStockPrice(stockPrice);
                        
                    } catch (Exception e) {
                        System.err.println("종목 " + item.getISU_NM() + " 저장 실패: " + e.getMessage());
                    }
                }
                
                System.out.println("KRX 데이터 동기화 완료");
            } else {
                System.out.println("KRX API 응답 데이터가 없습니다.");
            }
            
        } catch (Exception e) {
            System.err.println("KRX API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("KRX 데이터 동기화 실패", e);
        }
    }
    
    /**
     * 문자열을 날짜로 변환 (YYYYMMDD -> LocalDate)
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
    
    /**
     * 문자열을 BigDecimal로 변환 (쉼표 제거)
     */
    private BigDecimal parseBigDecimal(String str) {
        if (str == null || str.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(str.replace(",", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 문자열을 Long으로 변환 (쉼표 제거)
     */
    private Long parseLong(String str) {
        if (str == null || str.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(str.replace(",", ""));
        } catch (Exception e) {
            return 0L;
        }
    }
}

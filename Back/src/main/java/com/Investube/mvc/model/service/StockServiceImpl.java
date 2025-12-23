package com.Investube.mvc.model.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.Investube.mvc.model.dto.KrxIndexResponse;
import com.Investube.mvc.service.StockPriceFileService;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockDao stockDao;

    @Autowired
    private StockPriceFileService stockPriceFileService;

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
    public Stock getStockByName(String stockName) {
        return stockDao.selectStockByName(stockName);
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
     * KRX KOSPI 시장 주식 정보를 가져와서 DB에 저장 (최근 1년 데이터 - 초기 설정용)
     */
    @Transactional
    public void syncKospiStocks() {
        try {
            System.out.println("KOSPI 데이터 동기화 시작 (최근 365일)");

            // 최근 365일간의 데이터를 수집
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(365);

            int successCount = 0;
            int totalDays = 365;

            for (int i = 0; i < totalDays; i++) {
                LocalDate targetDate = startDate.plusDays(i);
                String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                try {
                    if (syncKospiStocksByDate(dateStr)) {
                        successCount++;
                    }

                    // API 호출 간격 (너무 빠르게 호출하지 않도록)
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.err.println("KOSPI " + dateStr + " 동기화 실패: " + e.getMessage());
                }
            }

            System.out.println("KOSPI 데이터 동기화 완료: " + successCount + "/" + totalDays + "일");

        } catch (Exception e) {
            System.err.println("KOSPI API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("KOSPI 데이터 동기화 실패", e);
        }
    }

    /**
     * 오늘 KOSPI 데이터만 수집 (스케줄러용)
     */
    @Transactional
    public void syncTodayKospiStocks() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        System.out.println("KOSPI 오늘 데이터 동기화: " + today);

        if (syncKospiStocksByDate(today)) {
            System.out.println("KOSPI 오늘 데이터 동기화 완료");
        } else {
            System.out.println("KOSPI 오늘 데이터 없음 (휴장일일 수 있음)");
        }
    }

    /**
     * 특정 날짜의 KOSPI 데이터 수집
     */
    private boolean syncKospiStocksByDate(String dateStr) {
        try {
            String apiUrl = "https://data-dbg.krx.co.kr/svc/apis/sto/stk_bydd_trd";

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> inBlock = new HashMap<>();
            inBlock.put("basDd", dateStr);
            requestBody.put("InBlock_1", inBlock);

            HttpHeaders headers = new HttpHeaders();
            headers.set("AUTH_KEY", krxApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<KrxStockResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST,
                    requestEntity, KrxStockResponse.class);

            KrxStockResponse response = responseEntity.getBody();

            if (response != null && response.getOutBlock_1() != null) {
                List<KrxStockResponse.KrxStockItem> items = response.getOutBlock_1();

                // JSON 파일 저장을 위한 종목별 데이터 수집
                Map<String, List<StockPrice>> stockPricesMap = new HashMap<>();
                Map<String, String> stockNamesMap = new HashMap<>();

                for (KrxStockResponse.KrxStockItem item : items) {
                    try {
                        // Stock 정보 저장/업데이트 (첫 번째 날짜에만)
                        Stock existingStock = stockDao.selectStockByCode(item.getISU_CD());
                        if (existingStock == null) {
                            Stock stock = new Stock();
                            stock.setStockCode(item.getISU_CD());
                            stock.setStockName(item.getISU_NM());
                            stock.setMarket("KOSPI");
                            stock.setIndustry(item.getSECT_TP_NM());
                            stockDao.insertStock(stock);
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

                        stockDao.insertStockPrice(stockPrice);

                        // JSON 파일용 데이터 수집
                        stockPricesMap.computeIfAbsent(item.getISU_CD(), k -> new ArrayList<>()).add(stockPrice);
                        stockNamesMap.putIfAbsent(item.getISU_CD(), item.getISU_NM());
                    } catch (Exception e) {
                        // 개별 종목 오류는 무시하고 계속 진행
                    }
                }

                // JSON 파일에 저장
                for (Map.Entry<String, List<StockPrice>> entry : stockPricesMap.entrySet()) {
                    String stockCode = entry.getKey();
                    List<StockPrice> prices = entry.getValue();
                    String stockName = stockNamesMap.get(stockCode);
                    stockPriceFileService.addPricesToFile(stockCode, stockName, prices);
                }

                System.out.println("KOSPI " + dateStr + ": " + items.size() + "개 종목 저장 (DB + JSON)");
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * KRX KOSDAQ 시장 주식 정보를 가져와서 DB에 저장 (최근 1년 데이터 - 초기 설정용)
     */
    @Transactional
    public void syncKosdaqStocks() {
        try {
            System.out.println("KOSDAQ 데이터 동기화 시작 (최근 365일)");

            // 최근 365일간의 데이터를 수집
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(365);

            int successCount = 0;
            int totalDays = 365;

            for (int i = 0; i < totalDays; i++) {
                LocalDate targetDate = startDate.plusDays(i);
                String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                try {
                    if (syncKosdaqStocksByDate(dateStr)) {
                        successCount++;
                    }

                    // API 호출 간격
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.err.println("KOSDAQ " + dateStr + " 동기화 실패: " + e.getMessage());
                }
            }

            System.out.println("KOSDAQ 데이터 동기화 완료: " + successCount + "/" + totalDays + "일");

        } catch (Exception e) {
            System.err.println("KOSDAQ API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("KOSDAQ 데이터 동기화 실패", e);
        }
    }

    /**
     * 오늘 KOSDAQ 데이터만 수집 (스케줄러용)
     */
    @Transactional
    public void syncTodayKosdaqStocks() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        System.out.println("KOSDAQ 오늘 데이터 동기화: " + today);

        if (syncKosdaqStocksByDate(today)) {
            System.out.println("KOSDAQ 오늘 데이터 동기화 완료");
        } else {
            System.out.println("KOSDAQ 오늘 데이터 없음 (휴장일일 수 있음)");
        }
    }

    @Override
    @Transactional
    public void syncTodayStockData() {
        System.out.println("=".repeat(60));
        System.out.println("오늘 주식 데이터 동기화 시작");
        System.out.println("=".repeat(60));

        syncTodayKospiStocks();
        syncTodayKosdaqStocks();

        System.out.println("=".repeat(60));
        System.out.println("오늘 주식 데이터 동기화 완료");
        System.out.println("=".repeat(60));
    }

    @Override
    @Transactional
    public void syncStockDataByDate(String dateStr) {
        System.out.println("=".repeat(60));
        System.out.println(dateStr + " 주식 데이터 동기화 시작");
        System.out.println("=".repeat(60));

        System.out.println("KOSPI " + dateStr + " 데이터 동기화");
        if (syncKospiStocksByDate(dateStr)) {
            System.out.println("KOSPI " + dateStr + " 데이터 동기화 완료");
        } else {
            System.out.println("KOSPI " + dateStr + " 데이터 없음 (휴장일일 수 있음)");
        }

        System.out.println("KOSDAQ " + dateStr + " 데이터 동기화");
        if (syncKosdaqStocksByDate(dateStr)) {
            System.out.println("KOSDAQ " + dateStr + " 데이터 동기화 완료");
        } else {
            System.out.println("KOSDAQ " + dateStr + " 데이터 없음 (휴장일일 수 있음)");
        }

        System.out.println("=".repeat(60));
        System.out.println(dateStr + " 주식 데이터 동기화 완료");
        System.out.println("=".repeat(60));
    }

    /**
     * 특정 날짜의 KOSDAQ 데이터 수집
     */
    private boolean syncKosdaqStocksByDate(String dateStr) {
        try {
            String apiUrl = "https://data-dbg.krx.co.kr/svc/apis/sto/ksq_bydd_trd";

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> inBlock = new HashMap<>();
            inBlock.put("basDd", dateStr);
            requestBody.put("InBlock_1", inBlock);

            HttpHeaders headers = new HttpHeaders();
            headers.set("AUTH_KEY", krxApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<KrxStockResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST,
                    requestEntity, KrxStockResponse.class);

            KrxStockResponse response = responseEntity.getBody();

            if (response != null && response.getOutBlock_1() != null) {
                List<KrxStockResponse.KrxStockItem> items = response.getOutBlock_1();

                // JSON 파일 저장을 위한 종목별 데이터 수집
                Map<String, List<StockPrice>> stockPricesMap = new HashMap<>();
                Map<String, String> stockNamesMap = new HashMap<>();

                for (KrxStockResponse.KrxStockItem item : items) {
                    try {
                        // Stock 정보 저장/업데이트 (첫 번째 날짜에만)
                        Stock existingStock = stockDao.selectStockByCode(item.getISU_CD());
                        if (existingStock == null) {
                            Stock stock = new Stock();
                            stock.setStockCode(item.getISU_CD());
                            stock.setStockName(item.getISU_NM());
                            stock.setMarket("KOSDAQ");
                            stock.setIndustry(item.getSECT_TP_NM());
                            stockDao.insertStock(stock);
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

                        stockDao.insertStockPrice(stockPrice);

                        // JSON 파일용 데이터 수집
                        stockPricesMap.computeIfAbsent(item.getISU_CD(), k -> new ArrayList<>()).add(stockPrice);
                        stockNamesMap.putIfAbsent(item.getISU_CD(), item.getISU_NM());
                    } catch (Exception e) {
                        // 개별 종목 오류는 무시하고 계속 진행
                    }
                }

                // JSON 파일에 저장
                for (Map.Entry<String, List<StockPrice>> entry : stockPricesMap.entrySet()) {
                    String stockCode = entry.getKey();
                    List<StockPrice> prices = entry.getValue();
                    String stockName = stockNamesMap.get(stockCode);
                    stockPriceFileService.addPricesToFile(stockCode, stockName, prices);
                }

                System.out.println("KOSDAQ " + dateStr + ": " + items.size() + "개 종목 저장 (DB + JSON)");
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
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

    /**
     * KRX 지수 정보 조회 (KOSPI, KOSDAQ, KRX 대표지수들)
     */
    @Override
    public List<KrxIndexResponse.KrxIndexItem> getKrxIndices() {
        try {
            // KST 기준으로 오늘부터 거슬러가며 가장 최근에 유효한 지수 데이터를 찾음
            final int maxLookbackDays = 7; // 필요시 값 변경 가능
            String apiUrl = "https://data-dbg.krx.co.kr/svc/apis/idx/krx_dd_trd";

            for (int i = 0; i < maxLookbackDays; i++) {
                String baseDate = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).minusDays(i)
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                try {
                    Map<String, Object> requestBody = new HashMap<>();
                    Map<String, String> inBlock = new HashMap<>();
                    inBlock.put("basDd", baseDate);
                    requestBody.put("InBlock_1", inBlock);

                    HttpHeaders headers = new HttpHeaders();
                    headers.set("AUTH_KEY", krxApiKey);
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

                    ResponseEntity<KrxIndexResponse> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST,
                            requestEntity, KrxIndexResponse.class);

                    KrxIndexResponse response = responseEntity.getBody();

                    if (response != null && response.getOutBlock_1() != null && !response.getOutBlock_1().isEmpty()) {
                        if (i > 0) {
                            System.out.println("KRX 지수: " + baseDate + " 기준 최근 유효 데이터 사용");
                        }
                        return response.getOutBlock_1();
                    }
                } catch (Exception inner) {
                    // 해당 날짜 실패 시 다음 날짜로 넘어감
                    System.err.println("KRX 지수 조회 실패 (date=" + baseDate + "): " + inner.getMessage());
                }
            }

            // 지정한 기간 내 유효한 데이터가 없으면 빈 리스트 반환
            return List.of();
        } catch (Exception e) {
            System.err.println("KRX 지수 정보 조회 전체 실패: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * DB에 저장된 주가 데이터를 JSON 파일로 내보내기
     * - 기존에 DB에 있는 데이터를 JSON 파일로 저장
     * - 서버 시작 시 호출하여 JSON 파일 초기화
     */
    @Override
    public void exportDbToJson() {
        try {
            System.out.println("DB 데이터를 JSON 파일로 내보내기 시작...");

            // 모든 종목 조회
            List<Stock> allStocks = stockDao.selectAllStocks();
            int exportedCount = 0;

            // 1년 전 날짜 계산
            String startDate = LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            for (Stock stock : allStocks) {
                try {
                    // 해당 종목의 1년치 가격 데이터 조회
                    List<StockPrice> prices = stockDao.selectPricesByStockCodeAndDateRange(
                            stock.getStockCode(), startDate, endDate);

                    if (prices != null && !prices.isEmpty()) {
                        // JSON 파일에 저장
                        stockPriceFileService.addPricesToFile(
                                stock.getStockCode(),
                                stock.getStockName(),
                                prices);
                        exportedCount++;
                    }
                } catch (Exception e) {
                    // 개별 종목 오류는 무시
                }
            }

            System.out.println("DB -> JSON 내보내기 완료: " + exportedCount + "/" + allStocks.size() + " 종목");

        } catch (Exception e) {
            System.err.println("DB -> JSON 내보내기 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.Investube.mvc.model.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void initImportFromJsonIfEmpty() {
        try {
            int count = stockDao.countStocks();
            if (count == 0) {
                System.out.println("[StockService] Stock 테이블이 비어있음 -> JSON import 실행");
                importStockPricesFromJson();
            }

            // market/industry가 null인 종목이 있으면 자동 업데이트
            int nullMarketCount = stockDao.countStocksWithNullMarket();
            if (nullMarketCount > 0) {
                System.out
                        .println("[StockService] market/industry가 null인 종목 " + nullMarketCount + "개 발견 -> KRX에서 업데이트");
                updateMissingStockInfo();
            }
        } catch (Exception e) {
            System.err.println("[StockService] 초기 데이터 로드 실패: " + e.getMessage());
        }
    }

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
     * JSON 파일 데이터를 DB로 적재 (초기 실행/복원용)
     */
    @Override
    @Transactional
    public Map<String, Integer> importStockPricesFromJson() {
        Map<String, Integer> stats = new HashMap<>();
        int filesProcessed = 0;
        int stocksUpserted = 0;
        int pricesUpserted = 0;

        try {
            List<String> stockCodes = stockPriceFileService.getAllStockCodes();
            int totalFiles = stockCodes.size();

            System.out.println("[JSON Import] 시작 (총 " + totalFiles + "개)");

            int fileIndex = 0;
            for (String code : stockCodes) {
                fileIndex++;
                filesProcessed++;
                int pricesAddedForFile = 0;
                List<StockPrice> batchList = new ArrayList<>();

                Map<String, Object> data = stockPriceFileService.loadFromFile(code);
                String stockName = Objects.toString(data.getOrDefault("stockName", code), code);

                // Stock이 없으면 생성
                if (stockDao.selectStockByCode(code) == null) {
                    Stock stock = new Stock();
                    stock.setStockCode(code);
                    stock.setStockName(stockName);
                    stock.setMarket(null);
                    stock.setIndustry(null);
                    try {
                        stockDao.insertStock(stock);
                        stocksUpserted++;
                    } catch (Exception ignored) {
                        // 중복/경합은 무시
                    }
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> prices = (List<Map<String, Object>>) data.get("prices");
                if (prices != null) {
                    for (Map<String, Object> priceMap : prices) {
                        LocalDate tradeDate = parseDateObject(priceMap.get("tradeDate"));
                        if (tradeDate == null) {
                            continue;
                        }

                        StockPrice stockPrice = new StockPrice();
                        stockPrice.setStockCode(code);
                        stockPrice.setTradeDate(tradeDate);
                        stockPrice.setOpenPrice(parseBigDecimalObject(priceMap.get("openPrice")));
                        stockPrice.setHighPrice(parseBigDecimalObject(priceMap.get("highPrice")));
                        stockPrice.setLowPrice(parseBigDecimalObject(priceMap.get("lowPrice")));
                        stockPrice.setClosePrice(parseBigDecimalObject(priceMap.get("closePrice")));
                        stockPrice.setVolume(parseLongObject(priceMap.get("volume")));
                        stockPrice.setMarketCap(parseLongObject(priceMap.get("marketCap")));
                        batchList.add(stockPrice);
                    }
                }

                if (!batchList.isEmpty()) {
                    try {
                        stockDao.insertStockPricesBatch(batchList);
                        pricesUpserted += batchList.size();
                        pricesAddedForFile = batchList.size();
                    } catch (Exception ignored) {
                        // 배치 실패 시 개별 처리로 폴백
                        for (StockPrice sp : batchList) {
                            try {
                                stockDao.insertStockPrice(sp);
                                pricesUpserted++;
                                pricesAddedForFile++;
                            } catch (Exception innerIgnored) {
                                // 개별 행 실패는 계속 진행
                            }
                        }
                    }
                }

                System.out.println("[JSON Import] (" + fileIndex + "/" + totalFiles + ") " + code
                        + " 완료 - 가격 " + pricesAddedForFile + "건 추가/업서트");
            }

            stats.put("filesProcessed", filesProcessed);
            stats.put("stocksUpserted", stocksUpserted);
            stats.put("pricesUpserted", pricesUpserted);

            System.out.println("[JSON Import] 완료: files=" + filesProcessed + ", stocks=" + stocksUpserted
                    + ", prices=" + pricesUpserted);
            return stats;
        } catch (Exception e) {
            System.err.println("JSON -> DB import 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("JSON import failed", e);
        }
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
                        } else {
                            // 기존 Stock의 market/industry가 없으면 업데이트
                            if (existingStock.getMarket() == null || existingStock.getIndustry() == null) {
                                existingStock.setMarket("KOSPI");
                                existingStock.setIndustry(item.getSECT_TP_NM());
                                stockDao.updateStock(existingStock);
                            }
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
                        } else {
                            // 기존 Stock의 market/industry가 없으면 업데이트
                            if (existingStock.getMarket() == null || existingStock.getIndustry() == null) {
                                existingStock.setMarket("KOSDAQ");
                                existingStock.setIndustry(item.getSECT_TP_NM());
                                stockDao.updateStock(existingStock);
                            }
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

    private LocalDate parseDateObject(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimalObject(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString().replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLongObject(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString().replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * KRX 지수 정보 조회 (KOSPI, KOSDAQ, KRX 대표지수들)
     */
    @Override
    public List<KrxIndexResponse.KrxIndexItem> getKrxIndices() {
        List<KrxIndexResponse.KrxIndexItem> allIndices = new ArrayList<>();
        final int maxLookbackDays = 7;

        try {
            // 여러 API 엔드포인트에서 지수 데이터 가져오기
            String[] apiUrls = {
                    "https://data-dbg.krx.co.kr/svc/apis/idx/kospi_dd_trd", // KOSPI 지수
                    "https://data-dbg.krx.co.kr/svc/apis/idx/kosdaq_dd_trd", // KOSDAQ 지수
                    "https://data-dbg.krx.co.kr/svc/apis/idx/krx_dd_trd" // KRX 지수
            };

            for (String apiUrl : apiUrls) {
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

                        if (response != null && response.getOutBlock_1() != null
                                && !response.getOutBlock_1().isEmpty()) {
                            if (i > 0) {
                                System.out.println("KRX 지수 (" + apiUrl + "): " + baseDate + " 기준 최근 유효 데이터 사용");
                            }
                            allIndices.addAll(response.getOutBlock_1());
                            break; // 해당 API에서 데이터 찾으면 다음 API로
                        }
                    } catch (Exception inner) {
                        // 해당 날짜 실패 시 다음 날짜로
                        System.err.println("지수 조회 실패 (" + apiUrl + ", " + baseDate + "): " + inner.getMessage());
                    }
                }
            }

            return allIndices;
        } catch (Exception e) {
            System.err.println("KRX 지수 정보 조회 전체 실패: " + e.getMessage());
            e.printStackTrace();
            return allIndices;
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

    /**
     * 누락된 market/industry 정보 업데이트
     * KRX API를 호출하여 DB의 null인 market/industry를 채움
     */
    @Override
    public void updateMissingStockInfo() {
        System.out.println("============================================================");
        System.out.println("누락된 종목 정보(market/industry) 업데이트 시작");
        System.out.println("============================================================");

        // 최근 영업일들을 순차적으로 시도 (오늘, 어제, 그제...)
        int updatedCount = 0;
        String[] datesToTry = new String[5];
        for (int i = 0; i < 5; i++) {
            datesToTry[i] = LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        try {
            // 여러 날짜로 시도 (영업일 찾기)
            for (String dateStr : datesToTry) {
                System.out.println("시도 중: " + dateStr);

                // KOSPI 데이터 가져오기
                int kospiUpdated = updateStocksFromKrx(dateStr, "KOSPI", "11");

                // KOSDAQ 데이터 가져오기
                int kosdaqUpdated = updateStocksFromKrx(dateStr, "KOSDAQ", "22");

                if (kospiUpdated > 0 || kosdaqUpdated > 0) {
                    updatedCount = kospiUpdated + kosdaqUpdated;
                    System.out.println("영업일 " + dateStr + " 데이터로 업데이트 완료!");
                    break;
                }
            }

            System.out.println("============================================================");
            System.out.println("종목 정보 업데이트 완료: " + updatedCount + "개 업데이트됨");
            System.out.println("============================================================");

        } catch (Exception e) {
            System.err.println("종목 정보 업데이트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * KRX API에서 데이터 가져와서 null인 market/industry 업데이트
     */
    private int updateStocksFromKrx(String dateStr, String marketName, String marketCode) {
        int updatedCount = 0;
        try {
            // KOSPI와 KOSDAQ는 다른 엔드포인트 사용
            String apiUrl = marketName.equals("KOSPI")
                    ? "https://data-dbg.krx.co.kr/svc/apis/sto/stk_bydd_trd"
                    : "https://data-dbg.krx.co.kr/svc/apis/sto/ksq_bydd_trd";

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
                for (KrxStockResponse.KrxStockItem item : response.getOutBlock_1()) {
                    try {
                        Stock existingStock = stockDao.selectStockByCode(item.getISU_CD());
                        if (existingStock != null &&
                                (existingStock.getMarket() == null || existingStock.getIndustry() == null)) {
                            existingStock.setMarket(marketName);
                            existingStock.setIndustry(item.getSECT_TP_NM());
                            stockDao.updateStock(existingStock);
                            updatedCount++;
                        }
                    } catch (Exception e) {
                        // 개별 종목 오류는 무시
                    }
                }
            }
            System.out.println(marketName + ": " + updatedCount + "개 종목 업데이트됨");
        } catch (Exception e) {
            System.err.println(marketName + " 정보 가져오기 실패: " + e.getMessage());
        }
        return updatedCount;
    }
}

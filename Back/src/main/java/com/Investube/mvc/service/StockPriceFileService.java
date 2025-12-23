package com.Investube.mvc.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dto.StockPrice;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 주가 데이터를 JSON 파일로 관리하는 서비스
 * - 1년치 데이터를 JSON 파일로 저장
 * - 없는 날짜만 업데이트
 */
@Service
public class StockPriceFileService {

    private static final String DATA_DIR = "data/stock_prices";
    private final ObjectMapper objectMapper;
    private final Path dataPath;

    public StockPriceFileService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 프로젝트 루트 기준 data 폴더 경로
        this.dataPath = Paths.get(DATA_DIR);

        // 디렉토리 생성
        try {
            Files.createDirectories(dataPath);
            System.out.println("[StockPriceFileService] 데이터 디렉토리 생성됨: " + dataPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[StockPriceFileService] 디렉토리 생성 실패: " + e.getMessage());
        }
    }

    /**
     * JSON 파일에서 주가 데이터 로드
     * 
     * @param stockCode 종목 코드
     * @return 주가 데이터 맵 (날짜 -> 가격정보)
     */
    public Map<String, Object> loadFromFile(String stockCode) {
        Path filePath = dataPath.resolve(stockCode + ".json");

        if (!Files.exists(filePath)) {
            return createEmptyData(stockCode);
        }

        try {
            String content = Files.readString(filePath);
            return objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            System.err.println("[StockPriceFileService] 파일 읽기 실패 (" + stockCode + "): " + e.getMessage());
            return createEmptyData(stockCode);
        }
    }

    /**
     * 주가 데이터를 JSON 파일에 저장
     * 
     * @param stockCode 종목 코드
     * @param data      저장할 데이터
     */
    public void saveToFile(String stockCode, Map<String, Object> data) {
        Path filePath = dataPath.resolve(stockCode + ".json");

        try {
            // 마지막 업데이트 시간 갱신
            data.put("lastUpdated", LocalDate.now().toString());

            String json = objectMapper.writeValueAsString(data);
            Files.writeString(filePath, json);
            System.out.println("[StockPriceFileService] 파일 저장 완료: " + filePath.getFileName());
        } catch (IOException e) {
            System.err.println("[StockPriceFileService] 파일 저장 실패 (" + stockCode + "): " + e.getMessage());
        }
    }

    /**
     * 파일에 저장된 날짜 목록 반환
     * 
     * @param stockCode 종목 코드
     * @return 저장된 날짜 Set
     */
    @SuppressWarnings("unchecked")
    public Set<String> getExistingDates(String stockCode) {
        Map<String, Object> data = loadFromFile(stockCode);
        List<Map<String, Object>> prices = (List<Map<String, Object>>) data.get("prices");

        if (prices == null) {
            return new HashSet<>();
        }

        return prices.stream()
                .map(p -> (String) p.get("tradeDate"))
                .filter(d -> d != null)
                .collect(Collectors.toSet());
    }

    /**
     * 새로운 주가 데이터를 파일에 추가
     * 
     * @param stockCode 종목 코드
     * @param stockName 종목명
     * @param newPrices 추가할 주가 데이터 리스트
     */
    @SuppressWarnings("unchecked")
    public void addPricesToFile(String stockCode, String stockName, List<StockPrice> newPrices) {
        if (newPrices == null || newPrices.isEmpty()) {
            return;
        }

        Map<String, Object> data = loadFromFile(stockCode);

        // 종목명 업데이트
        if (stockName != null && !stockName.isEmpty()) {
            data.put("stockName", stockName);
        }

        // 기존 가격 데이터 가져오기
        List<Map<String, Object>> prices = (List<Map<String, Object>>) data.get("prices");
        if (prices == null) {
            prices = new ArrayList<>();
            data.put("prices", prices);
        }

        // 기존 날짜 Set
        Set<String> existingDates = prices.stream()
                .map(p -> (String) p.get("tradeDate"))
                .filter(d -> d != null)
                .collect(Collectors.toSet());

        // 새 데이터 추가 (중복 제거)
        int addedCount = 0;
        for (StockPrice sp : newPrices) {
            String dateStr = sp.getTradeDate().toString();
            if (!existingDates.contains(dateStr)) {
                Map<String, Object> priceMap = new HashMap<>();
                priceMap.put("tradeDate", dateStr);
                priceMap.put("openPrice", sp.getOpenPrice());
                priceMap.put("highPrice", sp.getHighPrice());
                priceMap.put("lowPrice", sp.getLowPrice());
                priceMap.put("closePrice", sp.getClosePrice());
                priceMap.put("volume", sp.getVolume());
                priceMap.put("marketCap", sp.getMarketCap());
                prices.add(priceMap);
                addedCount++;
            }
        }

        if (addedCount > 0) {
            // 날짜순 정렬
            prices.sort((a, b) -> {
                String dateA = (String) a.get("tradeDate");
                String dateB = (String) b.get("tradeDate");
                return dateA.compareTo(dateB);
            });

            // 1년치만 유지 (365일 초과 데이터 제거)
            LocalDate oneYearAgo = LocalDate.now().minusYears(1);
            prices.removeIf(p -> {
                String dateStr = (String) p.get("tradeDate");
                LocalDate date = LocalDate.parse(dateStr);
                return date.isBefore(oneYearAgo);
            });

            saveToFile(stockCode, data);
            System.out.println("[StockPriceFileService] " + stockCode + ": " + addedCount + "개 데이터 추가됨");
        }
    }

    /**
     * 모든 종목 파일 목록 반환
     * 
     * @return 종목 코드 리스트
     */
    public List<String> getAllStockCodes() {
        List<String> codes = new ArrayList<>();

        File dir = dataPath.toFile();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    codes.add(name.substring(0, name.length() - 5)); // .json 제거
                }
            }
        }

        return codes;
    }

    /**
     * 빈 데이터 구조 생성
     */
    private Map<String, Object> createEmptyData(String stockCode) {
        Map<String, Object> data = new HashMap<>();
        data.put("stockCode", stockCode);
        data.put("stockName", "");
        data.put("lastUpdated", LocalDate.now().toString());
        data.put("prices", new ArrayList<>());
        return data;
    }
}

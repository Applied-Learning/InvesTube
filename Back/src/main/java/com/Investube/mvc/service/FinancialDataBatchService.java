package com.Investube.mvc.service;

import com.Investube.mvc.model.dao.FinancialDao;
import com.Investube.mvc.model.dao.StockDao;
import com.Investube.mvc.model.dto.FinancialData;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 재무 데이터 일괄 동기화 및 CSV 캐싱 서비스
 */
@Service
public class FinancialDataBatchService {

    @Autowired
    private FinancialService financialService;

    @Autowired
    private FinancialDao financialDao;

    @Autowired
    private StockDao stockDao;

    private static final String CSV_FILE_PATH = "data/financial_data_cache.csv";
    private static final String CSV_HEADER = "stock_code,fiscal_year,revenue,operating_profit,net_income," +
            "total_assets,total_equity,total_liabilities,operating_margin,roe,debt_ratio," +
            "revenue_growth_rate,operating_profit_growth_rate,per_ratio,pbr_ratio,fcf,data_source,last_updated";

    // 동기화 상태 추적
    private AtomicBoolean isSyncing = new AtomicBoolean(false);
    private AtomicInteger syncProgress = new AtomicInteger(0);
    private AtomicInteger syncTotal = new AtomicInteger(0);
    private String syncStatus = "IDLE";
    private List<String> syncErrors = new ArrayList<>();

    /**
     * 전체 종목 재무 데이터 동기화 (백그라운드)
     * 2년치 데이터를 동기화하여 성장률 계산 가능
     */
    @Async
    public void syncAllFinancialData() {
        if (isSyncing.get()) {
            System.out.println("[BatchSync] 이미 동기화가 진행 중입니다.");
            return;
        }

        isSyncing.set(true);
        syncProgress.set(0);
        syncErrors.clear();
        syncStatus = "RUNNING";

        try {
            List<Stock> allStocks = stockDao.selectAllStocks();
            syncTotal.set(allStocks.size());

            int currentYear = java.time.Year.now().getValue();
            int[] years = { currentYear - 2, currentYear - 1 }; // 2023, 2024

            System.out.println("[BatchSync] 전체 동기화 시작 - 총 " + allStocks.size() + "개 종목, " + years.length + "년치");
            long startTime = System.currentTimeMillis();

            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < allStocks.size(); i++) {
                Stock stock = allStocks.get(i);
                syncProgress.set(i + 1);

                for (int year : years) {
                    try {
                        financialService.syncFinancialData(stock.getStockCode(), year);
                        successCount++;

                        // Rate limit 방지 (초당 약 3회)
                        Thread.sleep(350);
                    } catch (Exception e) {
                        failCount++;
                        String error = stock.getStockCode() + "(" + year + "): " + e.getMessage();
                        if (syncErrors.size() < 100) { // 에러 최대 100개만 저장
                            syncErrors.add(error);
                        }
                    }
                }

                // 100개마다 진행 상황 로그
                if ((i + 1) % 100 == 0) {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    double rate = (i + 1.0) / elapsed;
                    int remaining = (int) ((allStocks.size() - i - 1) / rate);
                    System.out.printf("[BatchSync] 진행: %d/%d (%.1f%%) - 성공: %d, 실패: %d - 남은 시간: ~%d분%n",
                            i + 1, allStocks.size(), (i + 1.0) / allStocks.size() * 100,
                            successCount, failCount, remaining / 60);
                }
            }

            long totalTime = (System.currentTimeMillis() - startTime) / 1000;
            System.out.printf("[BatchSync] 전체 동기화 완료 - 성공: %d, 실패: %d, 소요시간: %d분 %d초%n",
                    successCount, failCount, totalTime / 60, totalTime % 60);

            // CSV로 저장
            exportToCsv();
            syncStatus = "COMPLETED";

        } catch (Exception e) {
            System.err.println("[BatchSync] 동기화 중 오류: " + e.getMessage());
            e.printStackTrace();
            syncStatus = "ERROR: " + e.getMessage();
        } finally {
            isSyncing.set(false);
        }
    }

    /**
     * 현재 DB의 재무 데이터를 CSV로 내보내기
     */
    public void exportToCsv() {
        try {
            File file = new File(CSV_FILE_PATH);
            file.getParentFile().mkdirs();

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8))) {

                writer.println(CSV_HEADER);

                // 모든 재무 데이터 조회
                List<Stock> allStocks = stockDao.selectAllStocks();
                int count = 0;

                for (Stock stock : allStocks) {
                    List<FinancialData> dataList = financialDao.getFinancialDataList(stock.getStockCode(), 5);
                    for (FinancialData data : dataList) {
                        writer.println(formatCsvLine(data));
                        count++;
                    }
                }

                System.out.println("[BatchSync] CSV 내보내기 완료: " + count + "건 → " + CSV_FILE_PATH);
            }
        } catch (Exception e) {
            System.err.println("[BatchSync] CSV 내보내기 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * CSV 파일에서 DB로 로드
     */
    public int loadFromCsv() {
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            System.out.println("[BatchSync] CSV 파일 없음: " + CSV_FILE_PATH);
            return 0;
        }

        int loadedCount = 0;
        int skippedCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // 헤더 스킵
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    FinancialData data = parseCsvLine(line);
                    if (data != null) {
                        // 이미 존재하는지 확인
                        FinancialData existing = financialDao.getFinancialData(
                                data.getStockCode(), data.getFiscalYear(), null);

                        if (existing == null) {
                            financialDao.insertFinancialData(data);
                            loadedCount++;
                        } else {
                            skippedCount++;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[BatchSync] CSV 라인 파싱 오류: " + e.getMessage());
                }
            }

            System.out.printf("[BatchSync] CSV 로드 완료: %d건 추가, %d건 스킵 (이미 존재)%n",
                    loadedCount, skippedCount);
        } catch (Exception e) {
            System.err.println("[BatchSync] CSV 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }

        return loadedCount;
    }

    /**
     * FinancialData를 CSV 라인으로 변환
     */
    private String formatCsvLine(FinancialData data) {
        return String.join(",",
                nullSafe(data.getStockCode()),
                String.valueOf(data.getFiscalYear()),
                nullSafe(data.getRevenue()),
                nullSafe(data.getOperatingProfit()),
                nullSafe(data.getNetIncome()),
                nullSafe(data.getTotalAssets()),
                nullSafe(data.getTotalEquity()),
                nullSafe(data.getTotalLiabilities()),
                nullSafe(data.getOperatingMargin()),
                nullSafe(data.getRoe()),
                nullSafe(data.getDebtRatio()),
                nullSafe(data.getRevenueGrowthRate()),
                nullSafe(data.getOperatingProfitGrowthRate()),
                nullSafe(data.getPerRatio()),
                nullSafe(data.getPbrRatio()),
                nullSafe(data.getFcf()),
                nullSafe(data.getDataSource()),
                data.getLastUpdated() != null ? data.getLastUpdated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : "");
    }

    /**
     * CSV 라인을 FinancialData로 파싱
     */
    private FinancialData parseCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 17)
            return null;

        FinancialData data = new FinancialData();
        data.setStockCode(parts[0]);
        data.setFiscalYear(Integer.parseInt(parts[1]));
        data.setRevenue(parseLong(parts[2]));
        data.setOperatingProfit(parseLong(parts[3]));
        data.setNetIncome(parseLong(parts[4]));
        data.setTotalAssets(parseLong(parts[5]));
        data.setTotalEquity(parseLong(parts[6]));
        data.setTotalLiabilities(parseLong(parts[7]));
        data.setOperatingMargin(parseBigDecimal(parts[8]));
        data.setRoe(parseBigDecimal(parts[9]));
        data.setDebtRatio(parseBigDecimal(parts[10]));
        data.setRevenueGrowthRate(parseBigDecimal(parts[11]));
        data.setOperatingProfitGrowthRate(parseBigDecimal(parts[12]));
        data.setPerRatio(parseBigDecimal(parts[13]));
        data.setPbrRatio(parseBigDecimal(parts[14]));
        data.setFcf(parseLong(parts[15]));
        data.setDataSource(parts[16].isEmpty() ? "CSV" : parts[16]);
        if (parts.length > 17 && !parts[17].isEmpty()) {
            data.setLastUpdated(LocalDateTime.parse(parts[17], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            data.setLastUpdated(LocalDateTime.now());
        }

        return data;
    }

    private String nullSafe(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private Long parseLong(String s) {
        if (s == null || s.isEmpty())
            return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String s) {
        if (s == null || s.isEmpty())
            return null;
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 동기화 상태 조회 메서드들
    public boolean isSyncing() {
        return isSyncing.get();
    }

    public int getSyncProgress() {
        return syncProgress.get();
    }

    public int getSyncTotal() {
        return syncTotal.get();
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public List<String> getSyncErrors() {
        return new ArrayList<>(syncErrors);
    }

    public boolean hasCsvCache() {
        return new File(CSV_FILE_PATH).exists();
    }
}

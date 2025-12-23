package com.Investube.mvc.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.Investube.mvc.model.service.StockService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class StockDataScheduler {

    @Autowired
    private StockService stockService;

    /**
     * 앱 시작 시 최신 주가 데이터 체크 및 자동 동기화
     */
    @jakarta.annotation.PostConstruct
    public void onStartup() {
        // 비동기로 실행 (앱 시작 지연 방지)
        new Thread(() -> {
            try {
                Thread.sleep(5000); // 다른 초기화 완료 대기
                checkAndSyncLatestData();
            } catch (Exception e) {
                System.err.println("[시작 시] 주가 동기화 체크 실패: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 빠진 날짜의 주가 데이터를 모두 채움
     * - DB에서 가장 최근 날짜 조회
     * - 그 다음날부터 목표 날짜까지 빠진 날짜만 동기화
     * - 주말은 건너뜀, DB에 없는 날짜만 API 호출
     */
    private void checkAndSyncLatestData() {
        try {
            // 목표 날짜 계산 (오늘 16시 이후면 오늘, 아니면 전일)
            LocalDate targetDate = calculateTargetDate();

            // DB에서 삼성전자의 가장 최근 가격 날짜 조회
            var latestPrice = stockService.getLatestPrice("005930");
            LocalDate lastDbDate;

            if (latestPrice != null && latestPrice.getTradeDate() != null) {
                // 이미 LocalDate 타입
                lastDbDate = latestPrice.getTradeDate();
                System.out.println("[시작 시] DB 최신 날짜: " + lastDbDate);
            } else {
                // DB에 데이터가 전혀 없으면 30일 전부터 시작
                lastDbDate = targetDate.minusDays(30);
                System.out.println("[시작 시] DB에 데이터 없음, 30일 전부터 동기화");
            }

            // 마지막 DB 날짜 다음날부터 목표 날짜까지 체크
            LocalDate checkDate = lastDbDate.plusDays(1);
            int syncCount = 0;

            System.out.println("[시작 시] 빠진 날짜 체크: " + checkDate + " ~ " + targetDate);

            while (!checkDate.isAfter(targetDate)) {
                // 주말 건너뛰기
                int dayOfWeek = checkDate.getDayOfWeek().getValue();
                if (dayOfWeek == 6 || dayOfWeek == 7) {
                    checkDate = checkDate.plusDays(1);
                    continue;
                }

                String checkDateStr = checkDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String krxDateStr = checkDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                // 해당 날짜 데이터가 DB에 있는지 체크
                var prices = stockService.getPricesByStockCodeAndDateRange("005930", checkDateStr, checkDateStr);

                if (prices == null || prices.isEmpty()) {
                    System.out.println("[시작 시] " + checkDateStr + " 데이터 없음 -> 동기화");
                    stockService.syncStockDataByDate(krxDateStr);
                    syncCount++;

                    // API 과부하 방지를 위한 짧은 대기
                    Thread.sleep(500);
                } else {
                    System.out.println("[시작 시] " + checkDateStr + " 데이터 이미 존재 -> 스킵");
                }

                checkDate = checkDate.plusDays(1);
            }

            if (syncCount == 0) {
                System.out.println("[시작 시] 모든 주가 데이터가 최신 상태입니다.");
            } else {
                System.out.println("[시작 시] 총 " + syncCount + "일치 데이터 동기화 완료!");
            }

        } catch (Exception e) {
            System.err.println("[시작 시] 주가 데이터 체크 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 목표 날짜 계산 (장 마감 여부에 따라)
     */
    private LocalDate calculateTargetDate() {
        LocalDate targetDate = LocalDate.now();
        int dayOfWeek = targetDate.getDayOfWeek().getValue();
        int currentHour = java.time.LocalTime.now().getHour();

        // 주말 처리
        if (dayOfWeek == 6)
            targetDate = targetDate.minusDays(1); // 토요일 -> 금요일
        else if (dayOfWeek == 7)
            targetDate = targetDate.minusDays(2); // 일요일 -> 금요일
        // 평일 + 장 마감 전(16시 이전)이면 전일
        else if (currentHour < 16) {
            targetDate = targetDate.minusDays(1);
            // 전일이 주말이면 금요일로 조정
            int prevDayOfWeek = targetDate.getDayOfWeek().getValue();
            if (prevDayOfWeek == 6)
                targetDate = targetDate.minusDays(1);
            else if (prevDayOfWeek == 7)
                targetDate = targetDate.minusDays(2);
        }

        return targetDate;
    }

    // 매일 한국시간(KST) 기준 오후 6시(장 마감) 실행
    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void runAfterMarketClose() {
        try {
            System.out.println(
                    "[Scheduler] 장 마감 처리 시작 - " + java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Seoul")));

            // 오늘 종목 시세 동기화 (KOSPI, KOSDAQ)
            stockService.syncTodayStockData();

            // 지수 정보도 한 번 호출하여 최신값을 가져오도록 함 (API가 당일 데이터를 제공하지 않으면 어제 데이터를 사용)
            stockService.getKrxIndices();

            System.out.println("[Scheduler] 장 마감 처리 완료");
        } catch (Exception e) {
            System.err.println("[Scheduler] 장 마감 업데이트 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

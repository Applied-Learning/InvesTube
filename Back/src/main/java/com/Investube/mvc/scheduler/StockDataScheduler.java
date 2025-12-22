package com.Investube.mvc.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.Investube.mvc.model.service.StockService;

@Component
public class StockDataScheduler {

    @Autowired
    private StockService stockService;

    // 매일 한국시간(KST) 기준 오후 6시(장 마감) 실행
    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void runAfterMarketClose() {
        try {
            System.out.println("[Scheduler] 장 마감 처리 시작 - " + java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Seoul")));

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

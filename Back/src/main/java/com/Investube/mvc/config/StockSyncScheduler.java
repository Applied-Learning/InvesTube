package com.Investube.mvc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.Investube.mvc.model.service.StockService;

@Component
public class StockSyncScheduler {

    @Autowired
    private StockService stockService;

    /**
     * 매일 오후 6시에 주식 데이터 자동 동기화
     * 주식 시장이 마감된 후 최신 데이터를 가져옵니다
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void syncStockDataDaily() {
        try {
            System.out.println("=".repeat(60));
            System.out.println("예약된 주식 데이터 동기화 시작 (매일 오후 6시)");
            System.out.println("=".repeat(60));
            
            stockService.syncStockDataFromDart();
            
            System.out.println("=".repeat(60));
            System.out.println("예약된 주식 데이터 동기화 완료!");
            System.out.println("=".repeat(60));
        } catch (Exception e) {
            System.err.println("예약된 주식 데이터 동기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 테스트용: 1시간마다 동기화 (필요시 주석 해제)
     * 실제 운영에서는 위의 매일 6시 동기화만 사용하는 것을 권장
     */
    // @Scheduled(fixedRate = 3600000) // 1시간 = 3600000ms
    // public void syncStockDataHourly() {
    //     try {
    //         System.out.println("시간별 주식 데이터 동기화 시작 (1시간마다)");
    //         stockService.syncStockDataFromDart();
    //         System.out.println("시간별 주식 데이터 동기화 완료!");
    //     } catch (Exception e) {
    //         System.err.println("시간별 주식 데이터 동기화 실패: " + e.getMessage());
    //     }
    // }
}

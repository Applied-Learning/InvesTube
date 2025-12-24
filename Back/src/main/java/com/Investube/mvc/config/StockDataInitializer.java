package com.Investube.mvc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.Investube.mvc.model.service.StockService;

@Component
@Order(1)
public class StockDataInitializer implements CommandLineRunner {

    @Autowired
    private StockService stockService;

    @Override
    public void run(String... args) throws Exception {
        try {
            // DB 연결이 완전히 초기화될 때까지 대기
            Thread.sleep(2000);

            // DB에 주식 데이터가 있는지 확인
            int stockCount = stockService.getAllStocks().size();

            if (stockCount == 0) {
                System.out.println("=".repeat(60));
                System.out.println("주식 데이터가 없습니다. KRX API에서 데이터를 가져옵니다...");
                System.out.println("=".repeat(60));

                stockService.syncStockDataFromDart();

                System.out.println("=".repeat(60));
                System.out.println("주식 데이터 초기화 완료!");
                System.out.println("=".repeat(60));
            } else {
                System.out.println("주식 데이터 " + stockCount + "개가 이미 존재합니다.");
            }
        } catch (Exception e) {
            System.err.println("주식 데이터 초기화 실패: " + e.getMessage());
            // 실패해도 서버는 정상 시작되도록 함
        }
    }
}

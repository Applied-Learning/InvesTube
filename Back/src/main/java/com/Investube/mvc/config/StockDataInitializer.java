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
            // StockService의 동기 초기화 메서드 호출
            // 이 메서드는 내부적으로 JSON 적재, KRX 업데이트, CSV 업종 보정 등을 순차적으로 수행함
            stockService.initializeStockData();

        } catch (Exception e) {
            System.err.println("주식 데이터 초기화 실패: " + e.getMessage());
            // 실패해도 서버는 정상 시작되도록 함
        }
    }
}

package com.Investube.mvc.config;

import com.Investube.mvc.service.FinancialDataBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 서버 시작 시 CSV 캐시에서 재무 데이터 자동 로드
 */
@Component
@Order(2)
public class FinancialDataLoader implements ApplicationRunner {

    @Autowired
    private FinancialDataBatchService batchService;

    @Override
    public void run(ApplicationArguments args) {
        if (batchService.hasCsvCache()) {
            System.out.println("[시작] CSV 캐시 파일 발견, 재무 데이터 로드 시작...");
            long startTime = System.currentTimeMillis();

            int loadedCount = batchService.loadFromCsv();

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.printf("[시작] CSV 로드 완료: %d건, 소요시간: %.2f초%n",
                    loadedCount, elapsed / 1000.0);
        } else {
            System.out.println("[시작] CSV 캐시 파일 없음. 다음을 호출하여 동기화하세요:");
            System.out.println("       POST /financial/sync-all");
        }
    }
}

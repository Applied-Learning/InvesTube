package com.Investube.mvc.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.dto.StockPrice;
import com.Investube.mvc.model.dto.StockDetailDto;
import com.Investube.mvc.model.service.StockService;

@RestController
@RequestMapping("/stocks")
public class StockRestController {
    
    @Autowired
    private StockService stockService;
    
    // 전체 주식 목록 조회 (최신 가격 포함)
    @GetMapping
    public ResponseEntity<?> getStockList() {
        try {
            List<StockDetailDto> stocks = stockService.getStockListWithLatestPrice();
            return new ResponseEntity<>(stocks, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 상세 에러 출력
            return new ResponseEntity<>("Error: " + e.getMessage() + " - " + e.getClass().getName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 특정 주식 상세 정보 조회
    @GetMapping("/{stockCode}")
    public ResponseEntity<?> getStockDetail(@PathVariable String stockCode) {
        try {
            StockDetailDto stock = stockService.getStockDetail(stockCode);
            if (stock == null) {
                return new ResponseEntity<>("주식을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(stock, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 주식 가격 이력 조회
    @GetMapping("/{stockCode}/prices")
    public ResponseEntity<?> getStockPrices(@PathVariable String stockCode) {
        try {
            List<StockPrice> prices = stockService.getPricesByStockCode(stockCode);
            return new ResponseEntity<>(prices, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 특정 기간 주식 가격 조회
    @GetMapping("/{stockCode}/prices/range")
    public ResponseEntity<?> getStockPricesByDateRange(
            @PathVariable String stockCode,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<StockPrice> prices = stockService.getPricesByStockCodeAndDateRange(stockCode, startDate, endDate);
            return new ResponseEntity<>(prices, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 주식 등록
    @PostMapping
    public ResponseEntity<?> registerStock(@RequestBody Stock stock) {
        try {
            boolean result = stockService.registerStock(stock);
            if (result) {
                return new ResponseEntity<>("주식이 등록되었습니다.", HttpStatus.CREATED);
            }
            return new ResponseEntity<>("주식 등록에 실패했습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 주식 가격 등록/업데이트
    @PostMapping("/{stockCode}/prices")
    public ResponseEntity<?> registerStockPrice(@PathVariable String stockCode, @RequestBody StockPrice stockPrice) {
        try {
            stockPrice.setStockCode(stockCode);
            boolean result = stockService.registerStockPrice(stockPrice);
            if (result) {
                return new ResponseEntity<>("주가 정보가 저장되었습니다.", HttpStatus.CREATED);
            }
            return new ResponseEntity<>("주가 정보 저장에 실패했습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 주식 수정
    @PutMapping("/{stockCode}")
    public ResponseEntity<?> modifyStock(@PathVariable String stockCode, @RequestBody Stock stock) {
        try {
            stock.setStockCode(stockCode);
            boolean result = stockService.modifyStock(stock);
            if (result) {
                return new ResponseEntity<>("주식 정보가 수정되었습니다.", HttpStatus.OK);
            }
            return new ResponseEntity<>("주식 정보 수정에 실패했습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 주식 삭제
    @DeleteMapping("/{stockCode}")
    public ResponseEntity<?> removeStock(@PathVariable String stockCode) {
        try {
            boolean result = stockService.removeStock(stockCode);
            if (result) {
                return new ResponseEntity<>("주식이 삭제되었습니다.", HttpStatus.OK);
            }
            return new ResponseEntity<>("주식 삭제에 실패했습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // DART API 동기화
    @PostMapping("/sync/dart")
    public ResponseEntity<?> syncDartData() {
        try {
            stockService.syncStockDataFromDart();
            return new ResponseEntity<>("DART 데이터 동기화가 완료되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // KRX API 동기화
    @PostMapping("/sync/krx/{stockCode}")
    public ResponseEntity<?> syncKrxData(@PathVariable String stockCode) {
        try {
            stockService.syncStockPriceFromKrx(stockCode);
            return new ResponseEntity<>("KRX 데이터 동기화가 완료되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // KRX 지수 정보 조회
    @GetMapping("/indices")
    public ResponseEntity<?> getIndices() {
        try {
            return new ResponseEntity<>(stockService.getKrxIndices(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package com.Investube.mvc.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.dto.StockPrice;
import com.Investube.mvc.model.dto.StockDetailDto;
import com.Investube.mvc.model.dto.StockWish;
import com.Investube.mvc.model.dto.NewsArticle;
import com.Investube.mvc.model.service.StockService;
import com.Investube.mvc.model.service.StockWishService;
import com.Investube.mvc.model.service.NewsService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/stocks")
public class StockRestController {

    private static final Logger log = LoggerFactory.getLogger(StockRestController.class);

    @Autowired
    private StockService stockService;

    @Autowired
    private StockWishService stockWishService;

    @Autowired
    private NewsService newsService;

    // JWT에서 userId 추출
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        if (attr instanceof Integer) {
            return (Integer) attr;
        }
        return null;
    }

    // 전체 주식 목록 조회 (최신 가격 포함)
    @GetMapping
    public ResponseEntity<?> getStockList() {
        try {
            List<StockDetailDto> stocks = stockService.getStockListWithLatestPrice();
            return new ResponseEntity<>(stocks, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에서 에러 출력
            return new ResponseEntity<>("Error: " + e.getMessage() + " - " + e.getClass().getName(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
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

    @GetMapping("/{stockCode}/news")
    public ResponseEntity<?> getStockNews(
            @PathVariable String stockCode,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        try {
            Stock stock = stockService.getStockByCode(stockCode);
            if (stock == null) {
                return new ResponseEntity<>("주식을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }
            List<NewsArticle> news = newsService.searchNews(stock.getStockName(), stock.getStockCode(), limit);
            return new ResponseEntity<>(news, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to load news for stockCode={}", stockCode, e);
            // 뉴스 API/호출 오류 시 비즈니스 영향이 없으므로 빈 목록으로 응답
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
    }

    // 주식 가격이력 조회
    @GetMapping("/{stockCode}/prices")
    public ResponseEntity<?> getStockPrices(@PathVariable String stockCode) {
        try {
            List<StockPrice> prices = stockService.getPricesByStockCode(stockCode);
            return new ResponseEntity<>(prices, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 특정 기간 주식 가격조회
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
            return new ResponseEntity<>("DART 데이터가 동기화되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // KRX API 동기화
    @PostMapping("/sync/krx/{stockCode}")
    public ResponseEntity<?> syncKrxData(@PathVariable String stockCode) {
        try {
            stockService.syncStockPriceFromKrx(stockCode);
            return new ResponseEntity<>("KRX 데이터가 동기화되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // JSON -> DB import (initial load/restore)
    @PostMapping("/import/json")
    public ResponseEntity<?> importFromJson() {
        try {
            Map<String, Integer> result = stockService.importStockPricesFromJson();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // KRX indices
    @GetMapping("/indices")
    public ResponseEntity<?> getIndices() {
        try {
            return new ResponseEntity<>(stockService.getKrxIndices(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 누락된 market/industry 정보 업데이트
    @PostMapping("/update-stock-info")
    public ResponseEntity<?> updateMissingStockInfo() {
        try {
            stockService.updateMissingStockInfo();
            return new ResponseEntity<>("종목 정보(market/industry) 업데이트가 완료되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 기업 찜 관련 API ==========

    // 찜한 기업 목록 조회
    @GetMapping("/wished")
    public ResponseEntity<?> getWishedStocks(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            List<StockWish> wishedStocks = stockWishService.getWishedStocksByUserId(userId);
            // 찜한 종목마다 최신 주가가 포함된 상세 정보로 반환
            List<StockDetailDto> withPrices = new java.util.ArrayList<>();
            for (StockWish wish : wishedStocks) {
                StockDetailDto detail = stockService.getStockDetail(wish.getStockCode());
                if (detail != null) {
                    withPrices.add(detail);
                }
            }
            return new ResponseEntity<>(withPrices, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 찜 여부 확인
    @GetMapping("/{stockCode}/wished")
    public ResponseEntity<?> isWished(@PathVariable String stockCode, HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            boolean isWished = stockWishService.isWished(userId, stockCode);
            return new ResponseEntity<>(isWished, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 찜 추가
    @PostMapping("/{stockCode}/wish")
    public ResponseEntity<?> addWish(@PathVariable String stockCode, HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            boolean result = stockWishService.addWish(userId, stockCode);
            if (result) {
                return new ResponseEntity<>("찜이 추가되었습니다.", HttpStatus.OK);
            }
            return new ResponseEntity<>("찜 추가에 실패했습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 찜 삭제
    @DeleteMapping("/{stockCode}/wish")
    public ResponseEntity<?> removeWish(@PathVariable String stockCode, HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            boolean result = stockWishService.removeWish(userId, stockCode);
            if (result) {
                return new ResponseEntity<>("찜이 삭제되었습니다.", HttpStatus.OK);
            }
            return new ResponseEntity<>("찜 삭제에 실패했습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 기업 찜 개수 조회
    @GetMapping("/{stockCode}/wish-count")
    public ResponseEntity<?> getWishCount(@PathVariable String stockCode) {
        try {
            int count = stockWishService.getWishCount(stockCode);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

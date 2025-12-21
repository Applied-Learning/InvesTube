package com.Investube.mvc.model.service;

import com.Investube.mvc.model.dto.FinancialData;
import com.Investube.mvc.model.dto.InvestmentProfile;
import com.Investube.mvc.model.dto.StockWish;

import java.util.List;

/**
 * 재무 분석 통합 서비스 인터페이스
 * - DART API 동기화
 * - 재무 지표 계산
 * - 투자 점수 계산
 */
public interface FinancialService {
    
    /**
     * DART API에서 재무 데이터 동기화
     * @param stockCode 종목 코드
     * @param year 회계 연도
     * @return 저장된 재무 데이터
     */
    FinancialData syncFinancialData(String stockCode, int year) throws Exception;
    
    /**
     * 최신 재무 데이터 조회
     * @param stockCode 종목 코드
     * @return 최신 재무 데이터
     */
    FinancialData getLatestFinancialData(String stockCode);
    
    /**
     * 투자 프로필에 따른 점수 계산된 재무 데이터 조회
     * @param stockCode 종목 코드
     * @param profileId 투자 프로필 ID (null이면 기본 프로필 사용)
     * @return 점수가 계산된 재무 데이터
     */
    FinancialData getFinancialDataWithScore(String stockCode, Integer profileId);
    
    /**
     * 찜한 기업들의 재무 데이터 및 점수 일괄 조회
     * @param userId 사용자 ID
     * @param profileId 투자 프로필 ID (null이면 기본 프로필 사용)
     * @return 찜한 기업들의 재무 데이터 리스트 (점수 포함)
     */
    List<FinancialData> getWishedStocksWithScores(int userId, Integer profileId);
    
    /**
     * 특정 기업의 재무 데이터 이력 조회
     * @param stockCode 종목 코드
     * @return 재무 데이터 이력 리스트
     */
    List<FinancialData> getFinancialDataHistory(String stockCode);
}

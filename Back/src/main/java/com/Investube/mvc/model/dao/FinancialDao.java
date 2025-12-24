package com.Investube.mvc.model.dao;

import com.Investube.mvc.model.dto.FinancialData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FinancialDao {

        FinancialData getFinancialData(@Param("stockCode") String stockCode,
                        @Param("fiscalYear") int fiscalYear,
                        @Param("fiscalQuarter") Integer fiscalQuarter);

        FinancialData getLatestFinancialData(@Param("stockCode") String stockCode);

        List<FinancialData> getFinancialDataList(@Param("stockCode") String stockCode,
                        @Param("limit") int limit);

        int insertFinancialData(FinancialData financialData);

        int updateFinancialData(FinancialData financialData);

        int deleteFinancialData(@Param("financialId") int financialId);

        // 동종업계 재무 데이터 조회 (단일 업종)
        List<FinancialData> getFinancialDataByIndustry(@Param("industry") String industry);

        // 동종업계 재무 데이터 조회 (여러 업종 - 카테고리 기반)
        // 동종업계 재무 데이터 조회 (여러 업종 - 카테고리 기반)
        List<FinancialData> getFinancialDataByIndustries(@Param("industries") List<String> industries);
}

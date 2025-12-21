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
}

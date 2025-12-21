package com.Investube.mvc.model.dao;

import com.Investube.mvc.model.dto.InvestmentProfile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InvestmentProfileDao {
    
    List<InvestmentProfile> getProfilesByUserId(@Param("userId") int userId);
    
    InvestmentProfile getDefaultProfile(@Param("userId") int userId);
    
    InvestmentProfile getProfileById(@Param("profileId") int profileId);
    
    int insertProfile(InvestmentProfile profile);
    
    int updateProfile(InvestmentProfile profile);
    
    int clearDefaultProfile(@Param("userId") int userId);
    
    int deleteProfile(@Param("profileId") int profileId);
}

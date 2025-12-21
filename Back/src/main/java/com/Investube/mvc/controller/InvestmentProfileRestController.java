package com.Investube.mvc.controller;

import com.Investube.mvc.model.dao.InvestmentProfileDao;
import com.Investube.mvc.model.dto.InvestmentProfile;
import com.Investube.mvc.service.FinancialAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 투자 프로필 관리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/profiles")
public class InvestmentProfileRestController {

    @Autowired
    private InvestmentProfileDao investmentProfileDao;

    @Autowired
    private FinancialAnalysisService financialAnalysisService;

    /**
     * userId 추출 헬퍼 메서드
     */
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj instanceof Integer) {
            return (Integer) userIdObj;
        }
        return null;
    }

    /**
     * 사용자의 투자 프로필 목록 조회
     * GET /profiles
     */
    @GetMapping
    public ResponseEntity<?> getProfiles(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            List<InvestmentProfile> profiles = investmentProfileDao.getProfilesByUserId(userId);
            return new ResponseEntity<>(profiles, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 기본 프로필 조회
     * GET /profiles/default
     */
    @GetMapping("/default")
    public ResponseEntity<?> getDefaultProfile(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            InvestmentProfile profile = investmentProfileDao.getDefaultProfile(userId);
            if (profile == null) {
                // 기본 프로필이 없으면 균형형 생성
                profile = financialAnalysisService.createDefaultProfile("균형형");
                profile.setUserId(userId);
                profile.setIsDefault(true);
                investmentProfileDao.insertProfile(profile);
            }
            return new ResponseEntity<>(profile, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 프로필 조회
     * GET /profiles/{profileId}
     */
    @GetMapping("/{profileId}")
    public ResponseEntity<?> getProfile(@PathVariable int profileId) {
        try {
            InvestmentProfile profile = investmentProfileDao.getProfileById(profileId);
            if (profile == null) {
                return new ResponseEntity<>("프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(profile, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 투자 프로필 생성
     * POST /profiles
     */
    @PostMapping
    public ResponseEntity<?> createProfile(
            HttpServletRequest request,
            @RequestBody InvestmentProfile profile) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            profile.setUserId(userId);

            // 기본 프로필로 설정하면 다른 프로필의 기본 해제
            if (profile.getIsDefault() != null && profile.getIsDefault()) {
                investmentProfileDao.clearDefaultProfile(userId);
            }

            int result = investmentProfileDao.insertProfile(profile);
            if (result > 0) {
                return new ResponseEntity<>(profile, HttpStatus.CREATED);
            }
            return new ResponseEntity<>("프로필 생성에 실패했습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 투자 프로필 수정
     * PUT /profiles/{profileId}
     */
    @PutMapping("/{profileId}")
    public ResponseEntity<?> updateProfile(
            HttpServletRequest request,
            @PathVariable int profileId,
            @RequestBody InvestmentProfile profile) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            // 프로필 소유자 확인
            InvestmentProfile existing = investmentProfileDao.getProfileById(profileId);
            if (existing == null) {
                return new ResponseEntity<>("프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }
            if (!existing.getUserId().equals(userId)) {
                return new ResponseEntity<>("권한이 없습니다.", HttpStatus.FORBIDDEN);
            }

            profile.setProfileId(profileId);
            profile.setUserId(userId);

            // 기본 프로필로 변경 시 다른 프로필의 기본 해제
            if (profile.getIsDefault() != null && profile.getIsDefault()) {
                investmentProfileDao.clearDefaultProfile(userId);
            }

            int result = investmentProfileDao.updateProfile(profile);
            if (result > 0) {
                return new ResponseEntity<>(profile, HttpStatus.OK);
            }
            return new ResponseEntity<>("프로필 수정에 실패했습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 투자 프로필 삭제
     * DELETE /profiles/{profileId}
     */
    @DeleteMapping("/{profileId}")
    public ResponseEntity<?> deleteProfile(
            HttpServletRequest request,
            @PathVariable int profileId) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            // 프로필 소유자 확인
            InvestmentProfile existing = investmentProfileDao.getProfileById(profileId);
            if (existing == null) {
                return new ResponseEntity<>("프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }
            if (!existing.getUserId().equals(userId)) {
                return new ResponseEntity<>("권한이 없습니다.", HttpStatus.FORBIDDEN);
            }

            int result = investmentProfileDao.deleteProfile(profileId);
            if (result > 0) {
                return new ResponseEntity<>("프로필이 삭제되었습니다.", HttpStatus.OK);
            }
            return new ResponseEntity<>("프로필 삭제에 실패했습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 기본 투자 프로필 생성 (5가지 유형)
     * POST /profiles/defaults
     */
    @PostMapping("/defaults")
    public ResponseEntity<?> createDefaultProfiles(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            String[] profileTypes = {"안정형", "성장형", "균형형", "가치형", "현금흐름형"};
            
            for (int i = 0; i < profileTypes.length; i++) {
                InvestmentProfile profile = financialAnalysisService.createDefaultProfile(profileTypes[i]);
                profile.setUserId(userId);
                profile.setIsDefault(i == 2); // 균형형을 기본으로 설정
                investmentProfileDao.insertProfile(profile);
            }

            Map<String, String> result = new HashMap<>();
            result.put("message", "기본 프로필 5개가 생성되었습니다.");
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

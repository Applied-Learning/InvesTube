package com.Investube.mvc.controller;

import com.Investube.mvc.model.dao.InvestmentProfileDao;
import com.Investube.mvc.model.dto.InvestmentProfile;
import com.Investube.mvc.model.dto.InvestmentSurvey;
import com.Investube.mvc.service.FinancialAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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

            // 가중치가 없으면 profileName 기반으로 기본 가중치 설정
            if (profile.getWeightRevenueGrowth() == null && profile.getProfileName() != null) {
                InvestmentProfile defaultWeights = financialAnalysisService
                        .createDefaultProfile(profile.getProfileName());
                profile.setWeightRevenueGrowth(defaultWeights.getWeightRevenueGrowth());
                profile.setWeightOperatingMargin(defaultWeights.getWeightOperatingMargin());
                profile.setWeightRoe(defaultWeights.getWeightRoe());
                profile.setWeightDebtRatio(defaultWeights.getWeightDebtRatio());
                profile.setWeightFcf(defaultWeights.getWeightFcf());
                profile.setWeightPer(defaultWeights.getWeightPer());
                profile.setWeightPbr(defaultWeights.getWeightPbr());
            }

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

            String[] profileTypes = { "안정형", "성장형", "균형형", "가치형", "현금흐름형" };

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

    /**
     * 투자 성향 설문 질문 조회
     * GET /profiles/survey/questions
     */
    @GetMapping("/survey/questions")
    public ResponseEntity<?> getSurveyQuestions() {
        List<Map<String, Object>> questions = new ArrayList<>();

        // Q1: 투자 기간
        Map<String, Object> q1 = new HashMap<>();
        q1.put("id", 1);
        q1.put("question", "예상 투자 기간은 어느 정도인가요?");
        q1.put("options", List.of(
                Map.of("value", 1, "text", "1년 미만 (단기)"),
                Map.of("value", 2, "text", "1~3년 (중기)"),
                Map.of("value", 3, "text", "3년 이상 (장기)")));
        questions.add(q1);

        // Q2: 손실 감내 수준
        Map<String, Object> q2 = new HashMap<>();
        q2.put("id", 2);
        q2.put("question", "투자 원금의 손실을 어느 정도까지 감내할 수 있나요?");
        q2.put("options", List.of(
                Map.of("value", 1, "text", "10% 이내"),
                Map.of("value", 2, "text", "10~30% 정도"),
                Map.of("value", 3, "text", "30% 이상도 가능")));
        questions.add(q2);

        // Q3: 투자 목적
        Map<String, Object> q3 = new HashMap<>();
        q3.put("id", 3);
        q3.put("question", "주된 투자 목적은 무엇인가요?");
        q3.put("options", List.of(
                Map.of("value", 1, "text", "원금 보존이 가장 중요"),
                Map.of("value", 2, "text", "안정적인 수익 추구"),
                Map.of("value", 3, "text", "고수익 추구 (위험 감수)")));
        questions.add(q3);

        // Q4: 투자 경험
        Map<String, Object> q4 = new HashMap<>();
        q4.put("id", 4);
        q4.put("question", "주식 투자 경험은 어느 정도인가요?");
        q4.put("options", List.of(
                Map.of("value", 1, "text", "거의 없음 (1년 미만)"),
                Map.of("value", 2, "text", "보통 (1~3년)"),
                Map.of("value", 3, "text", "많음 (3년 이상)")));
        questions.add(q4);

        // Q5: 변동성 대응
        Map<String, Object> q5 = new HashMap<>();
        q5.put("id", 5);
        q5.put("question", "보유 종목이 20% 하락했을 때 어떻게 하시겠어요?");
        q5.put("options", List.of(
                Map.of("value", 1, "text", "즉시 손절매"),
                Map.of("value", 2, "text", "상황을 지켜봄"),
                Map.of("value", 3, "text", "추가 매수 기회로 활용")));
        questions.add(q5);

        // Q6: 주식 투자 비중
        Map<String, Object> q6 = new HashMap<>();
        q6.put("id", 6);
        q6.put("question", "전체 자산 중 주식 투자 비중은 어느 정도인가요?");
        q6.put("options", List.of(
                Map.of("value", 1, "text", "20% 이하"),
                Map.of("value", 2, "text", "20~50%"),
                Map.of("value", 3, "text", "50% 이상")));
        questions.add(q6);

        // Q7: 정보 수집 방식
        Map<String, Object> q7 = new HashMap<>();
        q7.put("id", 7);
        q7.put("question", "투자 결정 시 어떻게 정보를 수집하나요?");
        q7.put("options", List.of(
                Map.of("value", 1, "text", "전문가나 지인 추천"),
                Map.of("value", 2, "text", "뉴스와 시장 분석 참고"),
                Map.of("value", 3, "text", "직접 재무제표 분석")));
        questions.add(q7);

        // Q8: 기대 수익률
        Map<String, Object> q8 = new HashMap<>();
        q8.put("id", 8);
        q8.put("question", "1년 기준 기대하는 투자 수익률은?");
        q8.put("options", List.of(
                Map.of("value", 1, "text", "예금 이자 이상 (3~5%)"),
                Map.of("value", 2, "text", "연 10~20%"),
                Map.of("value", 3, "text", "연 30% 이상")));
        questions.add(q8);

        return new ResponseEntity<>(questions, HttpStatus.OK);
    }

    /**
     * 투자 성향 설문 결과 제출 및 프로필 생성
     * POST /profiles/survey/submit
     */
    @PostMapping("/survey/submit")
    public ResponseEntity<?> submitSurvey(
            HttpServletRequest request,
            @RequestBody InvestmentSurvey survey) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            // 설문 결과로 성향 결정
            String profileType = survey.determineProfileType();
            int totalScore = survey.getTotalScore();

            // 기존 기본 프로필 해제
            investmentProfileDao.clearDefaultProfile(userId);

            // 새 프로필 생성
            InvestmentProfile profile = financialAnalysisService.createDefaultProfile(profileType);
            profile.setUserId(userId);
            profile.setIsDefault(true);
            profile.setProfileName(profileType + " (설문 결과)");
            investmentProfileDao.insertProfile(profile);

            // 결과 반환
            Map<String, Object> result = new HashMap<>();
            result.put("totalScore", totalScore);
            result.put("profileType", profileType);
            result.put("profile", profile);
            result.put("message", String.format("설문 결과: %s (점수: %d점)", profileType, totalScore));

            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

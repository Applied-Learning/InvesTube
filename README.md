# InvesTube

주식·투자 관련 유튜브 영상, 커뮤니티 게시글, AI 재무 분석을 한 곳에서 다루는 통합 투자 플랫폼입니다.  
투자 영상 피드, 게시판, 주식 재무 분석, AI 챗봇, 마이페이지, 알림까지 한 번에 사용할 수 있습니다.

---

## 기술 스택

- **Backend**
  - Java, Spring Boot
  - MyBatis
  - JWT 기반 인증

- **Frontend**
  - Vite + Vue 3
  - Vue Router
  - Pinia
  - Axios (공통 HTTP 클라이언트)

- **AI / 외부 API**
  - OpenAI GPT-4.1-mini (AI 챗봇)
  - Google Gemini 2.5 Flash (AI 투자 분석)
  - DART API (재무제표 데이터)
  - YouTube API (영상 메타데이터)

---

## 주요 기능

### 동영상

- 투자 관련 유튜브 영상 목록 / 정렬 / 검색
- 영상 상세 페이지 (조회수, 찜 수, 평균 평점 등 메타 정보 표시)
- 찜(위시리스트) 기능 및 찜한 영상 목록
- 리뷰 작성 / 수정 / 삭제
- AI 투자 영상 검증 (영상 등록 시 투자 관련성 자동 판별)
- 마이페이지에서:
  - 내가 찜한 영상 미리보기 / 전체 보기
  - 내가 업로드한(등록한) 영상 미리보기 / 전체 보기
  - 내가 리뷰 남긴 영상 미리보기 / 전체 보기

### 게시판

- 투자 관련 자유 게시판
  - 게시글 작성 / 수정 / 삭제
  - 게시글 목록 / 상세 조회, 조회수 카운트
  - 댓글 작성 / 수정 / 삭제
  - 에디터 이미지 업로드 지원
- 마이페이지에서:
  - 내가 작성한 게시글 미리보기 / 전체 보기
  - 내가 댓글을 단 게시글 미리보기 (글 제목 + 내가 단 댓글 내용 요약)

### 주식 정보

- 주식 목록 조회 (최신 가격 포함)
- 주식 상세 페이지
  - 기업 기본 정보 (종목코드, 시장, 업종)
  - 주가 차트 및 가격 이력
  - 관련 뉴스 조회
- 주식 찜하기 및 찜한 기업 목록

### 재무 분석

- DART API 연동 재무제표 데이터 조회
  - 매출액, 영업이익, 당기순이익
  - 자산총계, 자본총계, 부채총계
  - ROE, 부채비율, 영업이익률, PER, PBR
- 투자 점수 계산 (0~100점)
  - 사용자 투자 성향별 가중치 적용
  - 업종별 맞춤 리스크 기준
- AI 투자 분석
  - Gemini 2.5 Flash 기반 분석
  - Rule-based 기본 점수 + AI 보정 (하이브리드 방식)
  - 동종업계 비교 분석
  - AI 분석 요약 및 리스크 레벨 표시

### 투자 성향 프로필

- 투자 성향 설문 (5가지 항목)
- 설문 결과 기반 투자 프로필 자동 생성
  - 안정형, 성장형, 균형형, 가치형, 현금흐름형
- 프로필별 재무 지표 가중치 차등 적용
- 프로필 목록 조회 / 생성 / 수정 / 삭제

### AI 챗봇

- GPT-4.1-mini 기반 투자 질문 응답
- 일반 투자 질문 / 종목별 질문 지원
- 현재 보고 있는 종목의 재무 데이터 자동 Context 반영
- 사용자 투자 성향 맞춤 응답
- 마스코트 감정 반응 시스템
  - `[[SUCCESS]]` : 긍정적 반응 😊
  - `[[SHOCK]]` : 부정적 반응 😨
  - `[[CAUTION]]` : 신중한 반응 🤔
- 안전 장치: 매수/매도 추천 금지, 단정적 표현 금지

### 마이페이지

- 내 프로필
  - 닉네임, 아이디, 프로필 이미지 표시
  - 프로필 이미지 업로드 / 삭제
  - 닉네임 변경

- 팔로우
  - 나를 팔로우하는 유저 / 내가 팔로우하는 유저 목록
  - 팔로우 / 언팔로우 기능

- 활동 탭
  - 영상 탭: 찜한 영상 / 업로드한 영상 / 리뷰 남긴 영상 미리보기
  - 게시판 탭: 내가 작성한 게시글 / 내가 댓글 단 게시글 미리보기
  - 주식 탭: 찜한 기업 목록 / 재무 점수 표시

- 투자 성향
  - 내 투자 프로필 조회
  - 투자 성향 설문 진행 / 재진행

- 계정 설정
  - 비밀번호 변경
  - 회원 탈퇴

### 유저 프로필

- 다른 유저의 프로필 페이지
  - 프로필 이미지, 닉네임, 아이디
  - 해당 유저가 올린 영상 미리보기 / 전체 보기
  - 해당 유저가 작성한 게시글 미리보기 / 전체 보기
  - 팔로우 / 언팔로우

### 알림

- 지원 알림 타입
  - `COMMENT` : 내 게시글에 댓글이 달렸을 때
  - `REVIEW`  : 내 영상에 리뷰가 달렸을 때
  - `FOLLOW`  : 누군가 나를 팔로우했을 때
  - `WISH`    : 누군가 내 영상을 찜했을 때

- 헤더 우측 알림 아이콘
  - 읽지 않은 알림 개수 배지 표시
  - 드롭다운에서 안 읽은 알림 목록 표시
  - 알림 클릭 시 관련 페이지로 이동 (영상 / 게시글 / 유저 프로필 / 마이페이지)

- 알림 설정
  - 영상 리뷰 알림 ON/OFF
  - 영상 찜 알림 ON/OFF
  - 게시판 댓글 알림 ON/OFF
  - 팔로우 알림 ON/OFF
  

---

## 폴더 구조 개요

### Back/

Spring Boot 기반 백엔드 애플리케이션.

- `controller`
  - `AuthRestController` : 인증/로그인 관련
  - `VideoRestController` : 동영상 목록, 상세, 찜, 리뷰 연관 API
  - `BoardRestController` : 게시판(게시글) 관련 API
  - `CommentController` : 게시글 댓글 관련 API
  - `ReviewRestController` : 영상 리뷰 관련 API
  - `FollowRestController` : 팔로우/언팔로우, 팔로우 목록
  - `NotificationRestController` : 알림 조회 / 읽음 처리 / 알림 설정
  - `UserRestController` : 유저 정보, 마이페이지 관련
  - `StockRestController` : 주식 목록, 상세, 가격, 찜 관련 API
  - `FinancialRestController` : 재무 데이터 조회, AI 분석, DART 동기화
  - `InvestmentProfileRestController` : 투자 성향 프로필 관리, 설문
  - `ChatRestController` : AI 챗봇 질문 응답

- `service`
  - `ChatService` : GPT 챗봇 프롬프트 생성 및 응답 처리
  - `AIAnalysisService` : Gemini 기반 AI 투자 분석
  - `FinancialAnalysisService` : 재무 지표 계산, 투자 점수 산출
  - `DartApiService` : DART API 연동, 재무제표 파싱
  - `PeerComparisonService` : 동종업계 비교 분석
  - `VideoAiService` : 영상 투자 관련성 AI 검증

- `model/dto`
  - `Video`, `Review`, `BoardPost`, `BoardComment`, `User`, `Notification` 등 도메인 DTO
  - `Stock`, `StockPrice`, `FinancialData` : 주식/재무 관련 DTO
  - `InvestmentProfile`, `InvestmentSurvey` : 투자 성향 관련 DTO
  - `AiAnalysisResult`, `PeerStats` : AI 분석 결과 DTO

- `util`
  - `JwtUtil` : JWT 발급/검증 유틸리티
  - `OpenAIApiClient` : OpenAI GPT API 클라이언트
  - `GeminiApiClient` : Google Gemini API 클라이언트
  - `AiPromptBuilder` : AI 프롬프트 생성 유틸리티
  - `IndustryRiskCriteria` : 업종별 리스크 판단 기준

- `config`
  - `WebConfig` : CORS, 인터셉터 설정
  - `SwaggerConfig` : Swagger / OpenAPI 설정
  - `MyBatisConfig` : MyBatis 설정

- `resources`
  - `schema.sql` : DB 스키마  
    (users, videos, reviews, board_post, board_comments, follow, notifications, notification_settings, stocks, stock_prices, financial_data, investment_profiles 등)
  - `mappers/*.xml` : MyBatis 매퍼들

### Front/

Vue 3 + Vite 기반 프론트엔드 애플리케이션.

- `src/api/`
  - `http.js` : Axios 인스턴스 (baseURL, JWT 헤더 포함)
  - `video.js`, `board.js`, `user.js`, `follow.js`, `comment.js`, `notification.js` 등 각 도메인별 API 래퍼
  - `stock.js`, `financial.js`, `profile.js`, `chat.js` : 주식/재무/AI 관련 API 래퍼

- `src/views/`
  - `VideoListView.vue` : 메인 영상 리스트
  - `VideoDetailView.vue` : 영상 상세 + 리뷰
  - `BoardListView.vue` : 게시판 목록
  - `BoardDetailView.vue` : 게시글 상세 + 댓글
  - `MyPageView.vue` : 마이페이지 (프로필, 활동, 설정, 알림 설정)
  - `MyVideosView.vue`, `MyBoardsView.vue`, `MyReviewedVideosView.vue` : 내 활동 상세 페이지들
  - `UserProfileView.vue`, `UserVideosView.vue`, `UserBoardsView.vue` : 다른 유저 프로필 및 활동
  - `StockListView.vue` : 주식 목록
  - `StockDetailView.vue` : 주식 상세 + 재무 분석 + AI 분석
  - `InvestmentSurveyView.vue` : 투자 성향 설문

- `src/components/common/`
  - `Header.vue` : 상단 네비게이션, 알림 아이콘 포함
  - `NotificationDropdown.vue` : 알림 드롭다운 UI
  - `FloatingChatbot.vue` : AI 챗봇 플로팅 UI + 마스코트
  - 그 외 공통 레이아웃 및 UI 컴포넌트

- `src/stores/`
  - `chatbotStore.js` : 챗봇 상태 관리 (현재 종목 컨텍스트 등)
  - `auth.js` : 인증 상태 관리
  - `user.js` : 사용자 정보 관리

---

## 환경 변수 설정

### Backend (application.properties)

```properties
# DART API
dart.api.key=YOUR_DART_API_KEY

# OpenAI / GMS API
gms.api.key=YOUR_GMS_API_KEY
gms.api.url=https://gms.ssafy.io/gmsapi/...
gms.model=gpt-4.1-mini

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/investube
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

---

## 실행 방법

### Backend

```bash
cd Back
./mvnw spring-boot:run
```

### Frontend

```bash
cd Front
npm install
npm run dev
```

---

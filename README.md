# InvesTube

주식/투자 관련 유튜브 영상과 커뮤니티 게시판을 한 곳에서 볼 수 있게 하는 서비스입니다.  
현재는 레이아웃/영상 카드 UI와 임시 데이터 기반의 홈(영상) 화면까지 구현된 상태입니다.

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
  - Axios (공통 http 인스턴스)

---

## 폴더 구조

- `Back/`  
  - Spring Boot 백엔드 프로젝트  
  - 주요 패키지:
    - `controller`: `AuthRestController`, `VideoRestController`, `BoardRestController`, `UserRestController` 등
    - `model/dto`: `Video`, `User` 등 도메인 DTO
    - `config`: `WebConfig`(CORS, 인터셉터), `SwaggerConfig`, `MyBatisConfig`
    - `util`: `JwtUtil`
  - `video_dataset.csv`: 유튜브 영상 메타데이터(임시 데이터셋)

- `Front/`  
  - Vue 3 + Vite 프론트엔드 프로젝트  
  - `src/api/http.js`: baseURL 및 JWT Authorization 헤더를 처리하는 Axios 인스턴스  
  - `src/layouts/AppLayout.vue`: 상단 헤더 + 좌측 사이드바 + 메인 컨텐츠 레이아웃  
  - `src/components/common/`
    - `Header.vue`: 로고(홈 이동), 우측 알림/로그인 버튼
    - `Sidebar.vue`: 좌측 고정 탭 (영상 / 게시판 / 투자)
    - `Container.vue`: 화면 폭 컨테이너
    - `Button.vue`, `Card.vue`, `Modal.vue`: 공통 UI 컴포넌트
  - `src/components/video/VideoCard.vue`  
    - 썸네일, 업로더 프로필(이름/아바타), 조회수/메타 정보, 찜 토글 버튼 포함 영상 카드
  - `src/views/`
    - `VideoListView.vue`: 홈 화면(영상 탭). CSV 기반 임시 데이터로 `VideoCard` 그리드 렌더링
    - `BoardListView.vue`: 게시판 목록 화면(초기 뼈대)
    - `InvestView.vue`: 투자 분석/추천 화면(초기 뼈대)
  - `src/router/index.js`
    - `/` → `VideoListView` (홈 = 영상 탭)
    - `/videos` → `/` 리다이렉트
    - `/board` → `BoardListView`
    - `/invest` → `InvestView`

---

## 현재 구현된 기능

- 백엔드
  - JWT 기반 로그인/회원가입 및 사용자 API
  - 영상 목록/상세/검색/찜, 게시판·댓글·팔로우·리뷰 API 기본 구현
  - MyBatis를 이용한 DB 연동 및 Swagger 기반 API 문서

- 프론트엔드
  - 상단 헤더 + 좌측 사이드바 + 메인 컨텐츠 레이아웃
  - 공통 UI 컴포넌트: Button, Card, Modal, Container, VideoCard
  - 홈(영상 탭)에서 임시 데이터셋 기반 영상 카드 리스트 렌더링
  - 게시판/투자 뷰 기본 뼈대 및 라우팅 구성


## 추후 작업 예정

- 인증 고도화
  - 로그인/회원가입 화면 구현 및 Pinia 연동
  - Spring Security 도입 및 JWT·권한 관리 정리

- 영상/게시판 기능 강화
  - API 연동 및 정렬·페이지네이션
  - 영상 상세 페이지 및 찜/리뷰 연동
  - 게시판 목록/상세/작성/수정/삭제 UI 구현

- 외부 Public API 활용
  - YouTube Data API, 금융/증권 관련 API 연동
  - API 활용하여 심화 기능 구현

---

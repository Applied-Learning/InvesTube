# InvesTube

주식·투자 관련 유튜브 영상과 커뮤니티 게시글을 한 곳에서 다루는 서비스입니다.  
투자 영상 피드, 게시판, 마이페이지, 알림까지 한 번에 사용할 수 있습니다.

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

---

## 주요 기능

### 동영상

- 투자 관련 유튜브 영상 목록 / 정렬 / 검색
- 영상 상세 페이지 (조회수, 찜 수, 평균 평점 등 메타 정보 표시)
- 찜(위시리스트) 기능 및 찜한 영상 목록
- 리뷰 작성 / 수정 / 삭제
- 마이페이지에서:
  - 내가 찜한 영상 미리보기 / 전체 보기
  - 내가 업로드한(등록한) 영상 미리보기 / 전체 보기
  - 내가 리뷰 남긴 영상 미리보기 / 전체 보기

### 게시판

- 투자 관련 자유 게시판
  - 게시글 작성 / 수정 / 삭제
  - 게시글 목록 / 상세 조회, 조회수 카운트
  - 댓글 작성 / 수정 / 삭제
- 마이페이지에서:
  - 내가 작성한 게시글 미리보기 / 전체 보기
  - 내가 댓글을 단 게시글 미리보기 (글 제목 + 내가 단 댓글 내용 요약)

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

- `model/dto`
  - `Video`, `Review`, `BoardPost`, `BoardComment`, `User`, `Notification` 등 도메인 DTO

- `config`
  - `WebConfig` : CORS, 인터셉터 설정
  - `SwaggerConfig` : Swagger / OpenAPI 설정
  - `MyBatisConfig` : MyBatis 설정

- `util`
  - `JwtUtil` : JWT 발급/검증 유틸리티

- `resources`
  - `schema.sql` : DB 스키마  
    (users, videos, reviews, board_post, board_comments, follow, notifications, notification_settings 등)
  - `mappers/*.xml` : MyBatis 매퍼들

### Front/

Vue 3 + Vite 기반 프론트엔드 애플리케이션.

- `src/api/`
  - `http.js` : Axios 인스턴스 (baseURL, JWT 헤더 포함)
  - `video.js`, `board.js`, `user.js`, `follow.js`, `comment.js`, `notification.js` 등 각 도메인별 API 래퍼

- `src/views/`
  - `VideoListView.vue` : 메인 영상 리스트
  - `VideoDetailView.vue` : 영상 상세 + 리뷰
  - `BoardListView.vue` : 게시판 목록
  - `BoardDetailView.vue` : 게시글 상세 + 댓글
  - `MyPageView.vue` : 마이페이지 (프로필, 활동, 설정, 알림 설정)
  - `MyVideosView.vue`, `MyBoardsView.vue`, `MyReviewedVideosView.vue` : 내 활동 상세 페이지들
  - `UserProfileView.vue`, `UserVideosView.vue`, `UserBoardsView.vue` : 다른 유저 프로필 및 활동

- `src/components/common/`
  - `Header.vue` : 상단 네비게이션, 알림 아이콘 포함
  - `NotificationDropdown.vue` : 알림 드롭다운 UI
  - 그 외 공통 레이아웃 및 UI 컴포넌트

---

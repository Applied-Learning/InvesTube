# SSAFIT - 운동 영상 추천 및 리뷰 서비스

Spring Boot + MyBatis 기반의 운동 영상 관리 및 추천 서비스

## 프로젝트 구조

```
src/main/java/com/Investube/mvc/
├── SsafitApplication.java          # Spring Boot 메인 클래스
├── controller/                      # Controller Layer (REST API)
│   ├── VideoRestController.java
│   ├── ReviewRestController.java
│   ├── UserRestController.java
│   └── WorkoutPlanRestController.java
├── model/
│   ├── dto/                        # DTO (Data Transfer Object)
│   │   ├── Video.java
│   │   ├── Review.java
│   │   ├── User.java
│   │   └── WorkoutPlan.java
│   ├── dao/                        # DAO (MyBatis Mapper Interface)
│   │   ├── VideoDao.java
│   │   ├── ReviewDao.java
│   │   ├── UserDao.java
│   │   └── WorkoutPlanDao.java
│   └── service/                    # Service Layer
│       ├── VideoService.java
│       ├── VideoServiceImpl.java
│       ├── ReviewService.java
│       ├── ReviewServiceImpl.java
│       ├── UserService.java
│       ├── UserServiceImpl.java
│       ├── WorkoutPlanService.java
│       └── WorkoutPlanServiceImpl.java
│
src/main/resources/
├── application.properties          # 데이터베이스 및 MyBatis 설정
├── mappers/                        # MyBatis XML Mapper
│   ├── video-mapper.xml
│   ├── review-mapper.xml
│   ├── user-mapper.xml
│   └── workoutplan-mapper.xml
└── schema.sql                      # 데이터베이스 스키마
```

## 주요 기능

### 1. 비디오 관리 (Video)
- 운동 영상 등록/수정/삭제
- 영상 목록 조회
- 운동 부위별 영상 조회 (상체, 하체, 전신 등)
- 카테고리별 영상 조회
- 키워드 검색
- 조회수 관리

### 2. 리뷰 관리 (Review)
- 영상별 리뷰 작성
- 리뷰 조회/수정/삭제
- 평점 시스템 (1-5점)

### 3. 사용자 관리 (User)
- 회원가입/로그인
- 사용자 정보 관리

### 4. 운동 계획 관리 (Workout Plan)
- 개인 운동 계획 생성
- 운동 계획 조회/수정/삭제
- 사용자별 운동 계획 관리

## API 엔드포인트

### Video API
- `GET /api/videos` - 전체 비디오 목록
- `GET /api/videos/{videoId}` - 특정 비디오 조회
- `POST /api/videos` - 비디오 등록
- `PUT /api/videos/{videoId}` - 비디오 수정
- `DELETE /api/videos/{videoId}` - 비디오 삭제
- `GET /api/videos/part/{part}` - 운동 부위별 조회
- `GET /api/videos/category/{category}` - 카테고리별 조회
- `GET /api/videos/search?keyword={keyword}` - 키워드 검색

### Review API
- `GET /api/reviews` - 전체 리뷰 목록
- `GET /api/reviews/{reviewId}` - 특정 리뷰 조회
- `GET /api/reviews/video/{videoId}` - 비디오별 리뷰 조회
- `POST /api/reviews` - 리뷰 등록
- `PUT /api/reviews/{reviewId}` - 리뷰 수정
- `DELETE /api/reviews/{reviewId}` - 리뷰 삭제

### User API
- `GET /api/users` - 전체 사용자 목록
- `GET /api/users/{userId}` - 특정 사용자 조회
- `POST /api/users/signup` - 회원가입
- `POST /api/users/login` - 로그인
- `PUT /api/users/{userId}` - 사용자 정보 수정
- `DELETE /api/users/{userId}` - 사용자 삭제

### Workout Plan API
- `GET /api/workout-plans` - 전체 운동 계획 목록
- `GET /api/workout-plans/{planId}` - 특정 운동 계획 조회
- `GET /api/workout-plans/user/{userId}` - 사용자별 운동 계획 조회
- `POST /api/workout-plans` - 운동 계획 등록
- `PUT /api/workout-plans/{planId}` - 운동 계획 수정
- `DELETE /api/workout-plans/{planId}` - 운동 계획 삭제

## 설정 방법

### 1. 데이터베이스 설정
`src/main/resources/application.properties` 파일에서 데이터베이스 연결 정보를 수정하세요:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ssafit?serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=ssafy
```

### 2. 데이터베이스 생성
`src/main/resources/schema.sql` 파일을 실행하여 데이터베이스와 테이블을 생성하세요.

### 3. 애플리케이션 실행
```bash
mvn spring-boot:run
```

또는 IDE에서 `SsafitApplication.java` 파일을 실행하세요.

## 기술 스택

- **Backend Framework**: Spring Boot 4.0.0
- **Java**: Java 17
- **Database**: MySQL
- **ORM**: MyBatis 3.0.3
- **Build Tool**: Maven

## MVC 패턴 구조

1. **Model (DTO + DAO + Service)**
   - DTO: 데이터 전송 객체
   - DAO: 데이터베이스 접근 인터페이스 (MyBatis Mapper)
   - Service: 비즈니스 로직 처리

2. **View**
   - REST API를 통한 JSON 응답

3. **Controller**
   - REST API 엔드포인트 제공
   - HTTP 요청/응답 처리

## 개발 참고사항

- 모든 API는 RESTful 방식으로 설계되었습니다
- MyBatis XML Mapper를 통해 SQL 쿼리를 관리합니다
- 서비스 계층에서 비즈니스 로직을 처리합니다
- 응답은 JSON 형식으로 반환됩니다

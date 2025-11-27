# InvesTube 프로젝트

## 개요
InvesTube는 **주식 투자**와 관련된 **영상 콘텐츠**를 제공하는 **웹 애플리케이션**입니다.  
이 애플리케이션에서는 **영상 조회**, **게시글 작성**, **댓글 및 리뷰 작성**, **찜 기능**, **팔로우 기능** 등을 제공합니다.  
사용자는 다양한 투자 관련 영상을 시청하고, 이에 대한 **리뷰**를 작성하며, **찜 목록**을 관리할 수 있습니다.

---

## 기술 스택

- **Backend**: Java, Spring Boot
- **Database**: MySQL
- **ORM**: MyBatis
- **Frontend**: Vue.js (추후 연동 예정)
- **기타**: Swagger (API 문서화)

---

## ERD (Entity Relationship Diagram)

### **ERD 테이블 구조**
![ERD](./images/erd.png)

## 기능 정의

### 1. **게시글 기능**
- **게시글 목록 조회**: 최신 게시글 목록을 조회합니다.
- **게시글 작성**: 사용자가 게시글을 작성할 수 있습니다.
- **게시글 상세 조회**: 게시글의 제목, 내용, 작성자 등을 조회합니다.
- **게시글 수정**: 작성자가 게시글을 수정할 수 있습니다.
- **게시글 삭제**: 작성자가 게시글을 삭제할 수 있습니다.

### 2. **댓글 기능**
- **댓글 목록 조회**: 게시글에 달린 댓글 목록을 조회합니다.
- **댓글 작성**: 사용자가 댓글을 작성할 수 있습니다.
- **댓글 수정**: 작성자가 댓글을 수정할 수 있습니다.
- **댓글 삭제**: 작성자가 댓글을 삭제할 수 있습니다.

### 3. **비디오 기능**
- **비디오 목록 조회**: 모든 비디오 목록을 조회합니다.
- **비디오 등록**: 사용자가 비디오를 등록할 수 있습니다.
- **비디오 상세 조회**: 비디오의 제목, 설명, 카테고리 등의 정보를 조회합니다.

### 4. **리뷰 기능**
- **리뷰 작성**: 사용자가 비디오에 대해 리뷰를 작성할 수 있습니다.
- **리뷰 조회**: 특정 비디오에 대한 리뷰 목록을 조회합니다.
- **리뷰 수정**: 작성자가 자신의 리뷰를 수정할 수 있습니다.
- **리뷰 삭제**: 작성자가 자신의 리뷰를 삭제할 수 있습니다.

### 5. **찜 기능**
- **찜한 영상 조회**: 사용자가 찜한 영상 목록을 조회합니다.
- **찜 토글**: 영상의 찜 상태를 토글(찜/취소)할 수 있습니다.

### 6. **팔로우 기능**
- **팔로우**: 유저가 다른 유저를 팔로우할 수 있습니다.
- **팔로워 조회**: 내가 팔로우한 유저를 조회합니다.
- **팔로잉 조회**: 나를 팔로우한 유저를 조회합니다.

---

## API 명세

### 1. **게시글 API**

- **게시글 목록 조회**
  - **Method**: `GET /board`
  - **Description**: 최신 게시글 목록을 조회합니다.

- **게시글 작성**
  - **Method**: `POST /board`
  - **Description**: 게시글을 작성합니다.

- **게시글 상세 조회**
  - **Method**: `GET /board/{post_id}`
  - **Description**: 게시글의 상세 정보를 조회합니다.

- **게시글 수정**
  - **Method**: `PUT /board/{post_id}`
  - **Description**: 게시글을 수정합니다.

- **게시글 삭제**
  - **Method**: `DELETE /board/{post_id}`
  - **Description**: 게시글을 삭제합니다.

### 2. **댓글 API**

- **댓글 목록 조회**
  - **Method**: `GET /board/{post_id}/comments`
  - **Description**: 게시글에 달린 댓글을 조회합니다.

- **댓글 작성**
  - **Method**: `POST /board/{post_id}/comments`
  - **Description**: 댓글을 작성합니다.

- **댓글 수정**
  - **Method**: `PUT /comments/{comment_id}`
  - **Description**: 댓글을 수정합니다.

- **댓글 삭제**
  - **Method**: `DELETE /comments/{comment_id}`
  - **Description**: 댓글을 삭제합니다.

### 3. **비디오 API**

- **비디오 목록 조회**
  - **Method**: `GET /videos`
  - **Description**: 모든 비디오 목록을 조회합니다.

- **비디오 등록**
  - **Method**: `POST /videos`
  - **Description**: 비디오를 등록합니다.

- **비디오 상세 조회**
  - **Method**: `GET /videos/{video_id}`
  - **Description**: 비디오의 상세 정보를 조회합니다.

### 4. **리뷰 API**

- **리뷰 작성**
  - **Method**: `POST /reviews`
  - **Description**: 비디오에 대한 리뷰를 작성합니다.

- **리뷰 조회**
  - **Method**: `GET /reviews/{video_id}`
  - **Description**: 비디오에 대한 리뷰 목록을 조회합니다.

- **리뷰 수정**
  - **Method**: `PUT /reviews/{review_id}`
  - **Description**: 작성자가 리뷰를 수정합니다.

- **리뷰 삭제**
  - **Method**: `DELETE /reviews/{review_id}`
  - **Description**: 작성자가 리뷰를 삭제합니다.

### 5. **찜 API**

- **찜한 영상 조회**
  - **Method**: `GET /videos?wish=true`
  - **Description**: 사용자가 찜한 영상 목록을 조회합니다.

- **찜 토글**
  - **Method**: `POST /videos/{video_id}/wish`
  - **Description**: 영상의 찜 상태를 토글(찜/취소)합니다.

### 6. **팔로우 API**

- **팔로우**
  - **Method**: `POST /follow/{user_id}`
  - **Description**: 유저가 다른 유저를 팔로우합니다.

- **팔로워 조회**
  - **Method**: `GET /users/{id}/followers`
  - **Description**: 팔로우한 사람들을 조회합니다.

- **팔로잉 조회**
  - **Method**: `GET /users/{id}/followings`
  - **Description**: 나를 팔로우한 사람들을 조회합니다.

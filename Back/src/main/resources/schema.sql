-- SSAFIT Database Schema

-- Create Database
CREATE DATABASE IF NOT EXISTS ssafit;
USE ssafit;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Videos Table
CREATE TABLE IF NOT EXISTS videos (
    video_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    channel_name VARCHAR(100) NOT NULL,
    video_url VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255),
    category VARCHAR(50),
    part VARCHAR(50) COMMENT '운동 부위: 상체, 하체, 전신, 복근, 팔, 다리 등',
    view_count INT DEFAULT 0,
    upload_date VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Reviews Table
CREATE TABLE IF NOT EXISTS reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    video_id INT NOT NULL,
    writer VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE
);

-- Workout Plans Table
CREATE TABLE IF NOT EXISTS workout_plans (
    plan_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date VARCHAR(50),
    end_date VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Sample Data for Videos
INSERT INTO videos (title, channel_name, video_url, thumbnail_url, category, part, view_count, upload_date) VALUES
('10분 전신 스트레칭', '힐링요가', 'https://youtube.com/watch?v=sample1', 'https://img.youtube.com/vi/sample1/0.jpg', '요가', '전신', 1500, '2024-01-15'),
('하체 근력 운동 루틴', '헬스트레이너김', 'https://youtube.com/watch?v=sample2', 'https://img.youtube.com/vi/sample2/0.jpg', '근력', '하체', 2300, '2024-01-20'),
('복근 만들기 5분 운동', '홈트왕', 'https://youtube.com/watch?v=sample3', 'https://img.youtube.com/vi/sample3/0.jpg', '근력', '복근', 3500, '2024-02-01'),
('상체 근력 강화 운동', '피트니스마스터', 'https://youtube.com/watch?v=sample4', 'https://img.youtube.com/vi/sample4/0.jpg', '근력', '상체', 1800, '2024-02-10');

-- Sample Data for Users
INSERT INTO users (username, password, email, nickname) VALUES
('ssafy', '1234', 'ssafy@ssafy.com', 'SSAFY 싸피'),
('user01', 'pass01', 'user01@test.com', '운동왕');

-- Sample Data for Reviews
INSERT INTO reviews (video_id, writer, title, content, rating, created_at) VALUES
(1, 'ssafy', '너무 좋아요', '스트레칭 따라하기 쉽고 효과도 좋습니다!', 5, NOW()),
(2, 'user01', '하체 운동 최고', '이 운동 하고 나서 다리에 힘이 생긴 것 같아요', 4, NOW());

-- Sample Data for Workout Plans
INSERT INTO workout_plans (user_id, plan_name, description, start_date, end_date, created_at) VALUES
(1, '3월 다이어트 계획', '매일 30분씩 유산소 운동', '2024-03-01', '2024-03-31', NOW()),
(2, '근력 강화 프로그램', '상체 하체 번갈아가며 주 5회', '2024-02-15', '2024-04-15', NOW());

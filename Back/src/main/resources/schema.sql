-- Database Schema

-- Create Database
DROP DATABASE IF EXISTS investube;
CREATE DATABASE IF NOT EXISTS investube;
USE investube;

-- Table: users

CREATE TABLE IF NOT EXISTS users (
user_id INT AUTO_INCREMENT PRIMARY KEY,
id VARCHAR(50) UNIQUE,
password VARCHAR(255),
email VARCHAR(100) UNIQUE,
nickname VARCHAR(50),
profile_image VARCHAR(255),
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO users (user_id, id, password, email, nickname, profile_image, created_at, updated_at) VALUES
(1, 'user1', 'password123', 'user1@example.com', 'JohnDoe', null, NOW(), NOW()),
(2, 'user2', 'password456', 'user2@example.com', 'JaneDoe', null, NOW(), NOW()),
(3, 'user3', 'password789', 'user3@example.com', 'AliceSmith', null, NOW(), NOW()),
(4, 'user4', 'password101', 'user4@example.com', 'BobJohnson', null, NOW(), NOW()),
(5, 'user5', 'password102', 'user5@example.com', 'CharlieBrown', null, NOW(), NOW());

-- Table: category

CREATE TABLE IF NOT EXISTS category (
category_id INT PRIMARY KEY,
name VARCHAR(50) UNIQUE,
parent_id INT NULL,
FOREIGN KEY (parent_id) REFERENCES category(category_id)
);

-- Category codes (대분류/중분류)
-- 1: 투자 교육, 2: 종목 분석, 3: 경제 동향
-- 11: 기초 교육, 12: 분석 방법, 13: 투자 전략
-- 21: 재무 분석, 22: 산업 분석, 23: 종목 추천
-- 31: 국내 경제, 32: 국제 경제

INSERT INTO category (category_id, name, parent_id) VALUES
(1, '투자 교육', NULL),
(2, '종목 분석', NULL),
(3, '경제 동향', NULL),
(11, '기초 교육', 1),
(12, '분석 방법', 1),
(13, '투자 전략', 1),
(21, '재무 분석', 2),
(22, '산업 분석', 2),
(23, '종목 추천', 2),
(31, '국내 경제', 3),
(32, '국제 경제', 3);

-- Table: videos

CREATE TABLE IF NOT EXISTS videos (
video_id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT,
youtube_video_id VARCHAR(50),
title VARCHAR(200),
thumbnail_url VARCHAR(500),
description TEXT,
category_id INT,
view_count INT DEFAULT 0,
wish_count INT DEFAULT 0,
review_count INT DEFAULT 0,
avg_rating DECIMAL(2,1) DEFAULT 0.0,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES users(user_id),
FOREIGN KEY (category_id) REFERENCES category(category_id)
);

INSERT INTO videos (video_id, user_id, youtube_video_id, title, thumbnail_url, description, category_id, view_count, wish_count, review_count, avg_rating, created_at, updated_at) VALUES
(1, 1, 'nqbKOvQ8x1s', '금융 투자 기초', 'https://i.ytimg.com/vi/nqbKOvQ8x1s/hqdefault.jpg', '이 영상은 금융 투자 기초에 대해 설명합니다.', 11, 1000, 1, 1, 4, NOW(), NOW()),
(2, 2, 'ROLPR_eIrVg', '기술의 발전과 미래', 'https://i.ytimg.com/vi/ROLPR_eIrVg/hqdefault.jpg', '기술의 발전이 사회에 미치는 영향에 대해 다룹니다.', 12, 2000, 1, 1, 5, NOW(), NOW()),
(3, 3, 'hm-tW-O4YXw', '투자 전략 분석', 'https://i.ytimg.com/vi/hm-tW-O4YXw/hqdefault.jpg', '이 영상에서는 투자 전략에 대해 다루고 있습니다.', 13, 3000, 1, 1, 5, NOW(), NOW()),
(4, 4, 'YWgI4_Az7f4', '경제 성장과 투자', 'https://i.ytimg.com/vi/YWgI4_Az7f4/hqdefault.jpg', '경제 성장에 따른 투자 기회를 분석합니다.', 31, 1200, 1, 1, 4, NOW(), NOW()),
(5, 5, '8H1B836CAcI', '블록체인 기술의 미래', 'https://i.ytimg.com/vi/8H1B836CAcI/hqdefault.jpg', '블록체인 기술의 발전과 미래 가능성에 대해 설명합니다.', 12, 5000, 1, 1, 5, NOW(), NOW());

-- Table: reviews

CREATE TABLE IF NOT EXISTS reviews (
review_id INT AUTO_INCREMENT PRIMARY KEY,
video_id INT,
user_id INT,
content TEXT,
rating DECIMAL(2,1),
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
FOREIGN KEY (user_id) REFERENCES users(user_id),
UNIQUE KEY unique_video_user (video_id, user_id)
);

INSERT INTO reviews (review_id, video_id, user_id, content, rating, created_at, updated_at) VALUES
(1, 1, 1, '이 영상은 정말 유익하고 도움이 됩니다.', 4, NOW(), NOW()),
(2, 2, 2, '기술 발전에 대한 통찰이 깊었습니다. 추천합니다.', 5, NOW(), NOW()),
(3, 3, 3, '투자 전략에 대한 내용이 매우 유익하고 실용적이었습니다.', 5, NOW(), NOW()),
(4, 4, 4, '경제 성장과 투자에 대한 좋은 관점을 제공해주었습니다.', 4, NOW(), NOW()),
(5, 5, 5, '블록체인 기술에 대해 깊이 있는 이해를 돕는 영상입니다.', 5, NOW(), NOW());

-- Table: video_wish

CREATE TABLE IF NOT EXISTS video_wish (
user_id INT,
video_id INT,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (user_id, video_id),
FOREIGN KEY (user_id) REFERENCES users(user_id),
FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE
);

INSERT INTO video_wish (user_id, video_id, created_at) VALUES
(1, 1, NOW()),
(1, 3, NOW()),
(2, 2, NOW()),
(2, 4, NOW()),
(3, 5, NOW());

-- Table: board_post

CREATE TABLE IF NOT EXISTS board_post (
post_id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT,
title VARCHAR(200),
content TEXT,
view_count INT DEFAULT 0,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES users(user_id)
);

INSERT INTO board_post (post_id, user_id, title, content, view_count, created_at, updated_at) VALUES
(1, 1, '투자 기초', '투자에 대한 기초적인 지식을 다룬 글입니다.', 20, NOW(), NOW()),
(2, 2, '기술 발전', '기술의 발전에 대해 분석한 글입니다.', 15, NOW(), NOW()),
(3, 3, '주식 투자 전략', '주식 투자 전략에 대해 설명한 글입니다.', 30, NOW(), NOW()),
(4, 4, '블록체인 혁명', '블록체인 기술의 발전과 전망을 다룬 글입니다.', 20, NOW(), NOW()),
(5, 5, '경제 성장과 투자', '경제 성장에 따른 투자 기회를 설명한 글입니다.', 10, NOW(), NOW());

-- Table: follow

CREATE TABLE IF NOT EXISTS follow (
follower_id INT,
following_id INT,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (follower_id, following_id),
FOREIGN KEY (follower_id) REFERENCES users(user_id),
FOREIGN KEY (following_id) REFERENCES users(user_id)
);

INSERT INTO follow (follower_id, following_id, created_at) VALUES
(1, 2, NOW()),
(1, 3, NOW()),
(2, 4, NOW()),
(3, 5, NOW()),
(4, 1, NOW());

-- Table: board_comments

CREATE TABLE IF NOT EXISTS board_comments (
comment_id INT AUTO_INCREMENT PRIMARY KEY,
post_id INT,
user_id INT,
content TEXT,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
FOREIGN KEY (post_id) REFERENCES board_post(post_id),
FOREIGN KEY (user_id) REFERENCES users(user_id)
);

INSERT INTO board_comments (comment_id, post_id, user_id, content, created_at, updated_at) VALUES
(1, 1, 2, '투자 기초에 대한 추가적인 설명이 필요합니다.', NOW(), NOW()),
(2, 2, 3, '기술 발전에 대한 통찰이 훌륭합니다.', NOW(), NOW()),
(3, 3, 4, '좋은 전략이네요! 실제로 시도해보고 싶습니다.', NOW(), NOW()),
(4, 4, 5, '블록체인 혁명에 대해 더 알고 싶습니다.', NOW(), NOW()),
(5, 5, 1, '경제 성장과 투자에 대한 설명이 매우 유익합니다.', NOW(), NOW());

-- Table: notifications

CREATE TABLE IF NOT EXISTS notifications (
id INT AUTO_INCREMENT PRIMARY KEY,
recipient_id INT NOT NULL,
actor_id INT,
type VARCHAR(50),
target_type VARCHAR(50),
target_id INT,
message VARCHAR(500),
is_read BOOLEAN DEFAULT FALSE,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (recipient_id) REFERENCES users(user_id),
FOREIGN KEY (actor_id) REFERENCES users(user_id)
);

-- Table: notification_settings

CREATE TABLE IF NOT EXISTS notification_settings (
user_id INT NOT NULL,
type VARCHAR(50) NOT NULL, -- COMMENT, REVIEW, FOLLOW, WISH
enabled BOOLEAN NOT NULL DEFAULT TRUE,
PRIMARY KEY (user_id, type),
FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Table: stock
CREATE TABLE IF NOT EXISTS stock (
stock_id INT AUTO_INCREMENT PRIMARY KEY,
stock_code VARCHAR(20) UNIQUE NOT NULL,
stock_name VARCHAR(100) NOT NULL,
market VARCHAR(20), -- KOSPI, KOSDAQ
industry VARCHAR(100),
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table: stock_price
CREATE TABLE IF NOT EXISTS stock_price (
price_id INT AUTO_INCREMENT PRIMARY KEY,
stock_code VARCHAR(20) NOT NULL,
trade_date DATE NOT NULL,
open_price DECIMAL(15,2),
high_price DECIMAL(15,2),
low_price DECIMAL(15,2),
close_price DECIMAL(15,2),
volume BIGINT,
market_cap BIGINT,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (stock_code) REFERENCES stock(stock_code) ON DELETE CASCADE,
UNIQUE KEY unique_stock_date (stock_code, trade_date)
);

-- Table: stock_wish (기업 찜)
CREATE TABLE IF NOT EXISTS stock_wish (
user_id INT,
stock_code VARCHAR(20),
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (user_id, stock_code),
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
FOREIGN KEY (stock_code) REFERENCES stock(stock_code) ON DELETE CASCADE
);

-- Insert sample stock data (샘플 데이터는 KRX API로 동기화합니다)
-- 데이터 동기화: POST /stocks/sync/dart

-- Table: financial_data (기업 재무 데이터 및 점수)
CREATE TABLE IF NOT EXISTS financial_data (
    financial_id INT AUTO_INCREMENT PRIMARY KEY,
    stock_code VARCHAR(20) NOT NULL,
    fiscal_year INT NOT NULL, -- 회계연도
    fiscal_quarter INT, -- 분기 (1,2,3,4) NULL이면 연간
    
    -- 재무제표 원본 데이터 (단위: 백만원)
    revenue BIGINT, -- 매출액
    operating_profit BIGINT, -- 영업이익
    net_income BIGINT, -- 당기순이익
    total_assets BIGINT, -- 총자산
    total_equity BIGINT, -- 자본총계
    total_liabilities BIGINT, -- 부채총계
    cash_flow_operating BIGINT, -- 영업활동 현금흐름
    cash_flow_investing BIGINT, -- 투자활동 현금흐름
    cash_flow_financing BIGINT, -- 재무활동 현금흐름
    
    -- 시장 데이터
    market_cap BIGINT, -- 시가총액
    stock_price DECIMAL(15,2), -- 주가
    shares_outstanding BIGINT, -- 발행주식수
    
    -- 계산된 재무 지표
    revenue_growth_rate DECIMAL(10,4), -- 매출 성장률 (%)
    operating_profit_growth_rate DECIMAL(10,4), -- 영업이익 성장률 (%)
    operating_margin DECIMAL(10,4), -- 영업이익률 (%)
    roe DECIMAL(10,4), -- ROE (%)
    debt_ratio DECIMAL(10,4), -- 부채비율 (%)
    fcf BIGINT, -- FCF (영업CF - 투자CF)
    per_ratio DECIMAL(10,4), -- PER
    pbr_ratio DECIMAL(10,4), -- PBR
    
    -- 종합 점수
    total_score DECIMAL(5,2), -- 0~100점
    
    -- 메타 정보
    data_source VARCHAR(50), -- DART, KRX 등
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (stock_code) REFERENCES stock(stock_code) ON DELETE CASCADE,
    UNIQUE KEY unique_financial_period (stock_code, fiscal_year, fiscal_quarter)
);

-- Table: investment_profiles (사용자별 투자 성향 및 가중치)
CREATE TABLE IF NOT EXISTS investment_profiles (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    profile_name VARCHAR(50) NOT NULL, -- 안정형, 성장형, 균형형, 공격형 등
    
    -- 각 지표별 가중치 (합계 100)
    weight_revenue_growth DECIMAL(5,2) DEFAULT 20.00, -- 매출 성장률 가중치
    weight_operating_margin DECIMAL(5,2) DEFAULT 20.00, -- 영업이익률 가중치
    weight_roe DECIMAL(5,2) DEFAULT 15.00, -- ROE 가중치
    weight_debt_ratio DECIMAL(5,2) DEFAULT 15.00, -- 부채비율 가중치
    weight_fcf DECIMAL(5,2) DEFAULT 15.00, -- FCF 가중치
    weight_per DECIMAL(5,2) DEFAULT 10.00, -- PER 가중치
    weight_pbr DECIMAL(5,2) DEFAULT 5.00, -- PBR 가중치
    
    is_default BOOLEAN DEFAULT FALSE, -- 기본 프로필 여부
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 기본 투자 성향 프리셋 (예시)
INSERT INTO investment_profiles 
    (user_id, profile_name, weight_revenue_growth, weight_operating_margin, weight_roe, weight_debt_ratio, weight_fcf, weight_per, weight_pbr, is_default) 
VALUES
    -- 안정형: 부채비율과 영업이익률 중시
    (1, '안정형', 10.00, 25.00, 15.00, 25.00, 15.00, 5.00, 5.00, TRUE),
    -- 성장형: 매출성장률과 ROE 중시
    (2, '성장형', 30.00, 15.00, 25.00, 10.00, 10.00, 5.00, 5.00, TRUE),
    -- 균형형: 모든 지표 균형
    (3, '균형형', 20.00, 20.00, 15.00, 15.00, 15.00, 10.00, 5.00, TRUE),
    -- 가치형: PER, PBR 중시
    (4, '가치형', 15.00, 15.00, 10.00, 15.00, 10.00, 20.00, 15.00, TRUE),
    -- 현금흐름형: FCF와 부채비율 중시
    (5, '현금흐름형', 15.00, 15.00, 10.00, 20.00, 30.00, 5.00, 5.00, TRUE);

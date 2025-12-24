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
(1, 'user1', '1234', 'user1@example.com', '김가영', '/uploads/user/user1_profile.png', NOW(), NOW()),
(2, 'user2', '1234', 'user2@example.com', '투자왕 김박사', '/uploads/user/user2_profile.png', NOW(), NOW()),
(3, 'user3', '1234', 'user3@example.com', '경제연구소', '/uploads/user/user3_profile.png', NOW(), NOW()),
(4, 'user4', '1234', 'user4@example.com', '뉴비', null, NOW(), NOW()),
(5, 'user5', '1234', 'user5@example.com', '주린이', null, NOW(), NOW());

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
(1, 1, 'nqbKOvQ8x1s', '초보자를 위한 주식투자 기초', 'https://i.ytimg.com/vi/nqbKOvQ8x1s/hqdefault.jpg', '주식투자 기초 개념을 쉽게 이해할 수 있습니다.', 11, 1000, 1, 2, 4.5, '2025-12-18', '2025-12-19'),
(2, 3, 'ROLPR_eIrVg', '나스닥 하락', 'https://i.ytimg.com/vi/ROLPR_eIrVg/hqdefault.jpg', '잘 나가던 나스닥 수직낙하한 진짜 이유', 32, 2000, 1, 2, 4.5, '2025-12-17', '2025-12-18'),
(3, 3, 'hm-tW-O4YXw', '되는 주식 감별법', 'https://i.ytimg.com/vi/hm-tW-O4YXw/hqdefault.jpg', '알짜배기 주식 알아보는 방법', 13, 3000, 1, 2, 4.0, '2025-12-14', '2025-12-16'),
(4, 2, 'YWgI4_Az7f4', '이 종목 추천합니다!', 'https://i.ytimg.com/vi/YWgI4_Az7f4/hqdefault.jpg', '이 주식만 계속 사모으세요', 23, 1200, 1, 2, 4.0, '2025-12-16', '2025-12-17'),
(5, 3, '8H1B836CAcI', '최근 주식시장 동향', 'https://i.ytimg.com/vi/8H1B836CAcI/hqdefault.jpg', '이 영상 보고 최근 상황을 알 수 있었습니다.', 31, 5000, 1, 2, 3.0, '2025-12-15', '2025-12-17'),
(6, 2, 'fLsA07eJ5vM', '삼성전자 & SK하이닉스 분석', 'https://i.ytimg.com/vi/fLsA07eJ5vM/hqdefault.jpg', '삼성전자와 SK하이닉스 주가 전망과 최근 동향을 분석', 22, 1500, 1, 2, 4.0, '2025-12-13', '2025-12-14'),
(7, 2, 'Sb5BfKiHlpY', '삼성전자 재무제표', 'https://i.ytimg.com/vi/Sb5BfKiHlpY/hqdefault.jpg', '삼성전자의 최신 재무제표 분석', 21, 1500, 1, 2, 4.5, '2025-12-11', '2025-12-12'),
(8, 2, '8T9lsaaPB_4', '2차전지 산업 전망', 'https://i.ytimg.com/vi/8T9lsaaPB_4/hqdefault.jpg', 'K-배터리 업계의 변화 분석 영상', 22, 1800, 1, 2, 4.5, '2025-12-10', '2025-12-11'),
(9, 1, 'hI3pzjLbOzY', '차트 보는 방법', 'https://i.ytimg.com/vi/hI3pzjLbOzY/hqdefault.jpg', '주식 초보자를 위한 필수 차트 분석 방법을 다룬 영상입니다.', 12, 1500, 1, 2, 4.0, '2025-12-12', '2025-12-13');

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
(1, 1, 4, '주식 투자의 기초를 잘 설명해주어서 매우 유익했습니다. 초보자에게 좋은 입문서가 될 것 같습니다.', 4, '2025-12-18', '2025-12-19'),
(2, 1, 5, '초보자에게 매우 유익한 영상입니다. 투자에 대한 기초적인 이해를 돕는 좋은 자료였습니다.', 5, '2025-12-17', '2025-12-18'),
(3, 2, 4, '나스닥 하락 원인과 경제적 배경을 잘 설명해주셨습니다. 투자자들에게 유익한 분석이었습니다.', 4, '2025-12-16', '2025-12-17'),
(4, 2, 5, '정확한 통찰을 제공해 주셔서 매우 유익했습니다. 경제적인 배경을 이해하는 데 큰 도움이 되었습니다.', 5, '2025-12-15', '2025-12-16'),
(5, 3, 3, '주식 감별법에 대한 설명은 좋았지만, 좀 더 깊이 있는 예시가 필요할 것 같습니다.', 3, '2025-12-14', '2025-12-15'),
(6, 3, 5, '주식 감별법에 대해 매우 유익한 정보를 얻었습니다. 초보자에게 특히 도움이 되는 내용입니다.', 5, '2025-12-13', '2025-12-14'),
(7, 4, 4, '추천할 만한 주식들에 대해 잘 설명해주었고, 실용적인 조언이 많았습니다.', 4, '2025-12-12', '2025-12-13'),
(8, 4, 3, '좀 더 구체적인 예시가 있었으면 더욱 좋았을 것 같습니다. 그래도 유익한 정보였습니다.', 3, '2025-12-11', '2025-12-12'),
(9, 5, 4, '최근 주식시장 동향을 알기 쉽게 설명해주었습니다. 다만, 더 많은 사례가 있었으면 좋았을 것 같습니다.', 4, '2025-12-10', '2025-12-11'),
(10, 5, 2, '주식시장 동향에 대한 내용이 조금 부족했어요. 좀 더 심층적인 분석이 필요할 것 같습니다.', 2, '2025-12-09', '2025-12-10'),
(11, 6, 4, '삼성전자와 SK하이닉스 주식에 대해 잘 설명되었습니다. 두 기업의 동향을 알기 쉽게 이해할 수 있었습니다.', 4, '2025-12-08', '2025-12-09'),
(12, 6, 5, '두 기업의 주식 동향을 분석한 내용이 매우 유익했습니다. 투자 결정을 내리는데 큰 도움이 되었습니다.', 5, '2025-12-07', '2025-12-08'),
(13, 7, 3, '삼성전자의 재무제표에 대해 다뤄주셨지만, 좀 더 깊이 있는 분석이 필요할 것 같습니다.', 3, '2025-12-06', '2025-12-07'),
(14, 7, 5, '재무제표 분석이 매우 잘 되어있어 유익했습니다. 주식 분석에 필요한 중요한 정보들이 잘 정리되었습니다.', 5, '2025-12-05', '2025-12-06'),
(15, 8, 4, '2차전지 산업 전망에 대해 잘 다뤄주셨습니다. K-배터리 산업의 성장 가능성에 대해 이해가 깊어졌습니다.', 4, '2025-12-04', '2025-12-05'),
(16, 8, 5, 'K-배터리 산업에 대한 매우 유익한 정보였습니다. 산업의 발전 방향을 잘 설명해주셔서 도움이 되었습니다.', 5, '2025-12-03', '2025-12-04'),
(17, 9, 3, '차트 분석 방법을 잘 설명해주셨지만, 예시가 더 많았으면 좋았을 것 같습니다. 초보자에게 유익한 내용이었습니다.', 3, '2025-12-02', '2025-12-03'),
(18, 9, 5, '차트 분석 방법에 대해 매우 유익한 정보를 얻었습니다. 초보자도 쉽게 이해할 수 있는 내용이었습니다.', 5, '2025-12-01', '2025-12-02');

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
(1, 1, '2025-12-12 10:30:00'),
(1, 2, '2025-12-13 14:20:00'),
(1, 3, '2025-12-14 16:00:00'),
(2, 3, '2025-12-14 10:00:00'),
(2, 4, '2025-12-15 09:00:00'),
(2, 5, '2025-12-16 11:00:00'),
(3, 1, '2025-12-13 15:30:00'),
(3, 5, '2025-12-14 12:45:00'),
(3, 7, '2025-12-15 13:00:00'),
(4, 2, '2025-12-12 09:30:00'),
(4, 6, '2025-12-13 10:00:00'),
(4, 8, '2025-12-14 11:00:00'),
(5, 3, '2025-12-13 10:45:00'),
(5, 4, '2025-12-14 12:00:00'),
(5, 9, '2025-12-15 14:00:00');

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
(1, 4, '주식 투자 초보자는 무엇을 시작해야 할까요?', '주식 투자를 시작하려면 어떤 기초부터 배우는 것이 좋을까요? 종목 선택과 전략 수립에 대해 고민이 많습니다.', 20, '2025-12-10 10:30:00', '2025-12-10 10:30:00'),
(2, 5, '기술 발전이 주식 시장에 미치는 영향은 무엇인가요?', '인공지능과 블록체인 기술이 주식 시장에 미치는 영향은 무엇일까요? 기술 혁신이 기업 가치에 어떤 영향을 미치는지 분석합니다.', 30, '2025-12-11 14:20:00', '2025-12-11 14:20:00'),
(3, 3, '주식 투자 전략: 장기 투자 vs 단기 투자', '장기 투자와 단기 투자에는 어떤 차이가 있을까요? 각 전략에 적합한 시장 환경과 리스크 관리 방법에 대해 논의해봅니다.', 40, '2025-12-12 16:05:00', '2025-12-12 16:05:00'),
(4, 2, '블록체인 기술의 발전과 향후 전망', '블록체인 기술이 앞으로 어떻게 발전할 것인가? 다양한 산업에서 블록체인이 어떻게 활용될 수 있는지에 대해 설명합니다.', 35, '2025-12-13 09:15:00', '2025-12-13 09:15:00'),
(5, 1, '주식 투자에 대한 다양한 의견: 무엇이 옳은가?', '장기 투자와 단기 투자, 가치 투자와 성장 투자 중 어떤 접근이 더 효과적일까요? 각 접근법에 대한 의견을 나눠봅시다.', 25, '2025-12-14 11:45:00', '2025-12-14 11:45:00');

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
(1, 2, '2025-12-12 10:30:00'),
(1, 3, '2025-12-13 14:20:00'),
(2, 4, '2025-12-14 16:00:00'),
(3, 5, '2025-12-15 09:45:00'),
(4, 1, '2025-12-16 12:00:00'),
(4, 2, '2025-12-16 13:00:00'),
(4, 3, '2025-12-17 08:30:00'),
(5, 1, '2025-12-17 14:10:00'),
(5, 2, '2025-12-17 14:45:00'),
(5, 3, '2025-12-18 15:00:00');

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
(1, 1, 1, '주식 투자를 시작할 때는 먼저 기본적인 투자 용어와 개념을 배우는 것이 중요합니다. 다양한 책과 온라인 자료를 활용해서 기본적인 이해를 쌓고, 작은 금액으로 시작해보세요.', '2025-12-12 12:30:00', '2025-12-12 12:30:00'),
(2, 1, 3, '주식 투자 초보자라면 분산 투자와 리스크 관리가 중요한 포인트입니다. 다양한 종목에 나누어 투자하면서 시장의 변동성에 대비하세요.', '2025-12-13 09:45:00', '2025-12-13 09:45:00'),
(3, 2, 2, '기술 발전은 주식 시장에 큰 영향을 미칩니다. 예를 들어, 인공지능과 블록체인 기술은 새로운 시장을 창출하고 기존 기업들의 사업 모델을 변화시키고 있습니다. 이러한 변화를 주목하는 것이 중요합니다.', '2025-12-13 10:15:00', '2025-12-13 10:15:00'),
(4, 3, 5, '이 글을 통해 주식 투자 전략에 대해 잘 배웠습니다. 전략을 세우는 데 많은 도움이 되었습니다.', '2025-12-14 15:30:00', '2025-12-14 15:30:00'),
(5, 4, 4, '블록체인 기술의 발전과 향후 전망에 대해 많은 것을 배웠습니다. 특히 블록체인이 다양한 산업에 어떻게 적용될 수 있는지에 대한 설명이 유익했습니다.', '2025-12-14 10:00:00', '2025-12-14 10:00:00'),
(6, 4, 5, '블록체인 기술에 대해 잘 배우고 갑니다. 금융 및 물류 분야에서의 활용 가능성에 대해 더 알게 되었습니다.', '2025-12-15 09:10:00', '2025-12-15 09:10:00'),
(7, 5, 2, '장기 투자와 단기 투자, 둘 다 장단점이 있겠지만, 저는 시장의 빠른 변화를 반영할 수 있는 단기 투자에 더 큰 가능성을 보고 있습니다. 시장 트렌드를 잘 읽는 것이 중요하죠.', '2025-12-16 08:25:00', '2025-12-16 08:25:00'),
(8, 5, 1, '단기 투자는 확실히 빠른 수익을 얻을 수 있지만, 리스크가 큰 부분도 있습니다. 저는 그에 대한 리스크 관리와 안정적인 투자 전략을 선호합니다.', '2025-12-16 10:05:00', '2025-12-16 10:05:00'),
(9, 5, 2, '리스크 관리는 정말 중요하죠. 그러나 단기 투자에서 발생하는 리스크를 잘 관리하면서도 기회를 포착하는 것이 투자자의 능력이라고 생각합니다. 물론, 장기 투자도 안정성이 높긴 합니다.', '2025-12-17 09:30:00', '2025-12-17 09:30:00'),
(10, 5, 1, '맞습니다. 리스크를 최소화하면서 단기 투자에서 기회를 잡는 것이 중요하지만, 장기 투자에서의 안정성을 놓치지 않도록 하는 것이 더 중요하다고 봅니다. 결국, 투자 전략은 개인의 성향과 시장 환경에 따라 다를 것입니다.', '2025-12-17 12:00:00', '2025-12-17 12:00:00');

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

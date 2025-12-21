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
category_id INT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(50) UNIQUE
);

INSERT INTO category (category_id, name) VALUES
(1, '금융'),
(2, '기술'),
(3, '투자');

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
(1, 1, 'nqbKOvQ8x1s', '금융 투자 기초', 'https://i.ytimg.com/vi/nqbKOvQ8x1s/hqdefault.jpg', '이 영상은 금융 투자 기초에 대해 설명합니다.', 1, 1000, 1, 1, 4, NOW(), NOW()),
(2, 2, 'ROLPR_eIrVg', '기술의 발전과 미래', 'https://i.ytimg.com/vi/ROLPR_eIrVg/hqdefault.jpg', '기술의 발전이 사회에 미치는 영향에 대해 다룹니다.', 2, 2000, 1, 1, 5, NOW(), NOW()),
(3, 3, 'hm-tW-O4YXw', '투자 전략 분석', 'https://i.ytimg.com/vi/hm-tW-O4YXw/hqdefault.jpg', '이 영상에서는 투자 전략에 대해 다루고 있습니다.', 3, 3000, 1, 1, 5, NOW(), NOW()),
(4, 4, 'YWgI4_Az7f4', '경제 성장과 투자', 'https://i.ytimg.com/vi/YWgI4_Az7f4/hqdefault.jpg', '경제 성장에 따른 투자 기회를 분석합니다.', 3, 1200, 1, 1, 4, NOW(), NOW()),
(5, 5, '8H1B836CAcI', '블록체인 기술의 미래', 'https://i.ytimg.com/vi/8H1B836CAcI/hqdefault.jpg', '블록체인 기술의 발전과 미래 가능성에 대해 설명합니다.', 2, 5000, 1, 1, 5, NOW(), NOW());

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
FOREIGN KEY (user_id) REFERENCES users(user_id)
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

-- Insert sample stock data (샘플 데이터는 KRX API로 동기화합니다)
-- 데이터 동기화: POST /stocks/sync/dart
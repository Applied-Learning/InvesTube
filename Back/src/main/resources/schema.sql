-- Database Schema

-- Create Database
CREATE DATABASE IF NOT EXISTS investube;
USE investube;

-- -----------------------------------------------------
-- Table: users
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  user_id        INT AUTO_INCREMENT PRIMARY KEY,
  id             VARCHAR(50) UNIQUE,
  password       VARCHAR(255),
  email          VARCHAR(100) UNIQUE,
  nickname       VARCHAR(50),
  profile_image  VARCHAR(255),
  created_at     DATETIME,
  updated_at     DATETIME
);

INSERT INTO users (user_id, id, password, email, nickname, profile_image, created_at, updated_at) VALUES
(1, 'user1', 'password123', 'user1@example.com', 'JohnDoe', 'https://example.com/profiles/user1.jpg', NOW(), NOW()),
(2, 'user2', 'password456', 'user2@example.com', 'JaneDoe', 'https://example.com/profiles/user2.jpg', NOW(), NOW()),
(3, 'user3', 'password789', 'user3@example.com', 'AliceSmith', 'https://example.com/profiles/user3.jpg', NOW(), NOW()),
(4, 'user4', 'password101', 'user4@example.com', 'BobJohnson', 'https://example.com/profiles/user4.jpg', NOW(), NOW()),
(5, 'user5', 'password102', 'user5@example.com', 'CharlieBrown', 'https://example.com/profiles/user5.jpg', NOW(), NOW());


-- -----------------------------------------------------
-- Table: category
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS category (
  category_id    INT AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(50) UNIQUE
);

INSERT INTO category (category_id, name) VALUES
(1, '금융'),
(2, '기술'),
(3, '투자');


-- -----------------------------------------------------
-- Table: videos
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS videos (
  video_id        INT AUTO_INCREMENT PRIMARY KEY,
  user_id         INT,
  youtube_video_id VARCHAR(50),
  title           VARCHAR(200),
  thumbnail_url   VARCHAR(500),
  description     TEXT,
  category_id     INT,
  view_count      INT DEFAULT 0,
  wish_count      INT DEFAULT 0,
  review_count    INT DEFAULT 0,
  avg_rating      DECIMAL(2,1) DEFAULT 0.0,
  created_at      DATETIME,
  updated_at      DATETIME,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (category_id) REFERENCES category(category_id)
);

INSERT INTO videos (video_id, user_id, youtube_video_id, title, thumbnail_url, description, category_id, view_count, wish_count, review_count, avg_rating, created_at, updated_at) VALUES
(1, 1, 'nqbKOvQ8x1s', '금융 투자 기초', 'https://i.ytimg.com/vi/nqbKOvQ8x1s/hqdefault.jpg', '이 영상은 금융 투자 기초에 대해 설명합니다.', 1, 1000, 150, 30, 4.5, NOW(), NOW()),
(2, 2, 'ROLPR_eIrVg', '기술의 발전과 미래', 'https://i.ytimg.com/vi/ROLPR_eIrVg/hqdefault.jpg', '기술의 발전이 사회에 미치는 영향에 대해 다룹니다.', 2, 2000, 200, 50, 4.8, NOW(), NOW()),
(3, 3, 'hm-tW-O4YXw', '투자 전략 분석', 'https://i.ytimg.com/vi/hm-tW-O4YXw/hqdefault.jpg', '이 영상에서는 투자 전략에 대해 다루고 있습니다.', 3, 3000, 250, 70, 4.7, NOW(), NOW()),
(4, 4, 'YWgI4_Az7f4', '경제 성장과 투자', 'https://i.ytimg.com/vi/YWgI4_Az7f4/hqdefault.jpg', '경제 성장에 따른 투자 기회를 분석합니다.', 3, 1200, 180, 40, 4.3, NOW(), NOW()),
(5, 5, '8H1B836CAcI', '블록체인 기술의 미래', 'https://i.ytimg.com/vi/8H1B836CAcI/hqdefault.jpg', '블록체인 기술의 발전과 미래 가능성에 대해 설명합니다.', 2, 5000, 300, 100, 4.9, NOW(), NOW());


-- -----------------------------------------------------
-- Table: reviews
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
  review_id     INT AUTO_INCREMENT PRIMARY KEY,
  video_id      INT,
  user_id       INT,
  content       TEXT,
  rating        DECIMAL(2,1),
  created_at    DATETIME,
  updated_at    DATETIME,
  FOREIGN KEY (video_id) REFERENCES videos(video_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

INSERT INTO reviews (review_id, video_id, user_id, content, rating, created_at, updated_at) VALUES
(1, 1, 1, '이 영상은 정말 유익하고 도움이 됩니다.', 4.5, NOW(), NOW()),
(2, 2, 2, '기술 발전에 대한 통찰이 깊었습니다. 추천합니다.', 5.0, NOW(), NOW()),
(3, 3, 3, '투자 전략에 대한 내용이 매우 유익하고 실용적이었습니다.', 4.7, NOW(), NOW()),
(4, 4, 4, '경제 성장과 투자에 대한 좋은 관점을 제공해주었습니다.', 4.3, NOW(), NOW()),
(5, 5, 5, '블록체인 기술에 대해 깊이 있는 이해를 돕는 영상입니다.', 5.0, NOW(), NOW());


-- -----------------------------------------------------
-- Table: video_wish
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS video_wish (
  user_id        INT,
  video_id       INT,
  created_at     DATETIME,
  PRIMARY KEY (user_id, video_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (video_id) REFERENCES videos(video_id)
);

INSERT INTO video_wish (user_id, video_id, created_at) VALUES
(1, 1, NOW()),
(1, 3, NOW()),
(2, 2, NOW()),
(2, 4, NOW()),
(3, 5, NOW());


-- -----------------------------------------------------
-- Table: board_post
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS board_post (
  post_id        INT AUTO_INCREMENT PRIMARY KEY,
  user_id        INT,
  title          VARCHAR(200),
  content        TEXT,
  created_at     DATETIME,
  updated_at     DATETIME,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

INSERT INTO board_post (post_id, user_id, title, content, created_at, updated_at) VALUES
(1, 1, '투자 기초', '투자에 대한 기초적인 지식을 다룬 글입니다.', NOW(), NOW()),
(2, 2, '기술 발전', '기술의 발전에 대해 분석한 글입니다.', NOW(), NOW()),
(3, 3, '주식 투자 전략', '주식 투자 전략에 대해 설명한 글입니다.', NOW(), NOW()),
(4, 4, '블록체인 혁명', '블록체인 기술의 발전과 전망을 다룬 글입니다.', NOW(), NOW()),
(5, 5, '경제 성장과 투자', '경제 성장에 따른 투자 기회를 설명한 글입니다.', NOW(), NOW());


-- -----------------------------------------------------
-- Table: board_post_images
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS board_post_images (
  image_id       INT AUTO_INCREMENT PRIMARY KEY,
  post_id        INT,
  image_url      VARCHAR(500),
  created_at     DATETIME,
  FOREIGN KEY (post_id) REFERENCES board_post(post_id)
);

INSERT INTO board_post_images (image_id, post_id, image_url, created_at) VALUES
(1, 1, 'https://example.com/images/investment.jpg', NOW()),
(2, 2, 'https://example.com/images/technology.jpg', NOW()),
(3, 3, 'https://example.com/images/stock_strategy.jpg', NOW()),
(4, 4, 'https://example.com/images/blockchain.jpg', NOW()),
(5, 5, 'https://example.com/images/economy_investment.jpg', NOW());


-- -----------------------------------------------------
-- Table: follow
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS follow (
  follower_id    INT,
  following_id   INT,
  created_at     DATETIME,
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


-- -----------------------------------------------------
-- Table: board_comments
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS board_comments (
  comment_id     INT AUTO_INCREMENT PRIMARY KEY,
  post_id        INT,
  user_id        INT,
  content        TEXT,
  created_at     DATETIME,
  updated_at     DATETIME,
  FOREIGN KEY (post_id) REFERENCES board_post(post_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

INSERT INTO board_comments (comment_id, post_id, user_id, content, created_at, updated_at) VALUES
(1, 1, 2, '투자 기초에 대한 추가적인 설명이 필요합니다.', NOW(), NOW()),
(2, 2, 3, '기술 발전에 대한 통찰이 훌륭합니다.', NOW(), NOW()),
(3, 3, 4, '좋은 전략이네요! 실제로 시도해보고 싶습니다.', NOW(), NOW()),
(4, 4, 5, '블록체인 혁명에 대해 더 알고 싶습니다.', NOW(), NOW()),
(5, 5, 1, '경제 성장과 투자에 대한 설명이 매우 유익합니다.', NOW(), NOW());

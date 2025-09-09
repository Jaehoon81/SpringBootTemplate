-- springframeworkdemo DB의 users Table Definition
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE `users`
ADD COLUMN `displayname` VARCHAR(50) NOT NULL DEFAULT '미입력' AFTER `password`;

-- springframeworkdemo DB의 blacklisted_tokens Table Definition
CREATE TABLE `blacklisted_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `token` VARCHAR(512) NOT NULL UNIQUE, -- 블랙리스트에 추가할 JWT 토큰 (길이 부족 시 TEXT 타입 고려)
    `expires_at` DATETIME NOT NULL,       -- 해당 JWT의 원래 만료 시간
    `blacklisted_at` DATETIME DEFAULT CURRENT_TIMESTAMP -- 블랙리스트에 추가된 시간
);

-- springframeworkdemo DB의 blacklisted_tokens Table Definition
CREATE TABLE `blacklisted_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `token` VARCHAR(512) NOT NULL UNIQUE, -- 블랙리스트에 추가할 JWT 토큰 (길이 부족 시 TEXT 타입 고려)
    `expires_at` DATETIME NOT NULL,       -- 해당 JWT의 원래 만료 시간
    `blacklisted_at` DATETIME DEFAULT CURRENT_TIMESTAMP -- 블랙리스트에 추가된 시간
);

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
ADD COLUMN `displayname` VARCHAR(50) NOT NULL UNIQUE AFTER `password`;

ALTER TABLE `users`
ADD COLUMN `email` VARCHAR(100) NOT NULL DEFAULT '' AFTER `displayname`, -- 이메일 주소 필수 항목으로 추가
ADD COLUMN `req_message` TEXT AFTER `email`,     -- 요청 메시지 추가 (NULL 허용)
ADD COLUMN `adminname` VARCHAR(50) AFTER `role`; -- 담당 관리자 이름 추가 (NULL 허용)

ALTER TABLE `users`
ADD COLUMN `is_approved` TINYINT(1) NOT NULL DEFAULT 0 AFTER `adminname`; -- 0: 미승인, 1: 승인

ALTER TABLE `users`
ADD COLUMN `active_session_jti` VARCHAR(255) NULL AFTER `is_approved`; -- 가장 최근에 발급된 모바일 JWT의 Jti(JWT ID)를 저장

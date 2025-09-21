-- springframeworkdemo DB의 roles Table Definition
CREATE TABLE `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `rolename` VARCHAR(20) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL, -- 권한(역할)에 대한 설명 추가
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO `roles` (`rolename`, `description`)
VALUES ('SYSTEM', '시스템 관리자'), ('ADMIN', '관리자'), ('USER', '일반 사용자');

-- springframeworkdemo DB의 users Table Definition
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `displayname` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL DEFAULT '', -- 이메일 주소 필수 항목으로 추가
    `role_id` BIGINT NOT NULL,
    `active_session_jti` VARCHAR(255) NULL, -- 가장 최근에 발급된 모바일 JWT의 Jti(JWT ID)를 저장
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE `users`
ADD CONSTRAINT `fk_users_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`);

-- springframeworkdemo DB의 approval_requests Table Definition
CREATE TABLE `approval_requests` (
    `request_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE, -- 한 사용자는 하나의 활성 요청만
    `req_message` TEXT NULL,
    `assigned_admin_id` BIGINT NULL, -- ADMIN의 users.id 참조
    `is_approved` TINYINT(1) NOT NULL DEFAULT 0, -- 0: 미승인, 1: 승인 (요청이 있을 경우에만 해당)
    `requested_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `approved_at` DATETIME NULL, -- 승인 완료 시 업데이트
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE, -- 사용자 삭제 시 요청도 삭제
    FOREIGN KEY (`assigned_admin_id`) REFERENCES `users`(`id`)
);

-- springframeworkdemo DB의 blacklisted_tokens Table Definition
CREATE TABLE `blacklisted_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `token` VARCHAR(512) NOT NULL UNIQUE, -- 블랙리스트에 추가할 JWT 토큰 (길이 부족 시 TEXT 타입 고려)
    `expires_at` DATETIME NOT NULL, -- 해당 JWT의 원래 만료 시간
    `blacklisted_at` DATETIME DEFAULT CURRENT_TIMESTAMP -- 블랙리스트에 추가된 시간
);

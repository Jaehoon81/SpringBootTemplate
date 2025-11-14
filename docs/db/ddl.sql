-- springboottemplate Database Creation
CREATE DATABASE `springboottemplate`; -- 생성할 데이터베이스의 이름을 명시하기 위해 기록

-- springboottemplate DB의 roles Table Definition
CREATE TABLE `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `rolename` VARCHAR(20) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL, -- 권한(역할)에 대한 설명 추가
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- springboottemplate DB의 users Table Definition
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `displayname` VARCHAR(50) NOT NULL UNIQUE,
    `profile_picture_path` VARCHAR(255) NULL,
    `email` VARCHAR(100) NOT NULL DEFAULT '',
    `role_id` BIGINT NOT NULL,
    `active_session_jti` VARCHAR(255) NULL, -- 가장 최근에 발급된 모바일 JWT의 Jti(JWT ID)를 저장
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0, -- 회원탈퇴 여부 0: 유지, 1: 탈퇴 (요청이 있을 경우에만 해당)
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE `users`
ADD CONSTRAINT `fk_users_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`);

-- CREATE FULLTEXT INDEX `idx_fulltext_displayname` ON `users`(`displayname`) WITH PARSER ngram;
ALTER TABLE `users`
ADD FULLTEXT `idx_fulltext_displayname` (`displayname`) WITH PARSER ngram;

-- springboottemplate DB의 approval_requests Table Definition
CREATE TABLE `approval_requests` (
    `request_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE, -- 한 사용자는 하나의 활성 요청만
    `req_message` TEXT NULL,
    `assigned_admin_id` BIGINT NULL, -- ADMIN의 users.id 참조
    `is_approved` TINYINT(1) NOT NULL DEFAULT 0, -- 계정승인 여부 0: 대기, 1: 승인 (요청이 있을 경우에만 해당)
    `requested_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `approved_at` DATETIME NULL -- 승인 완료 시 업데이트
);

ALTER TABLE `approval_requests`
ADD CONSTRAINT `fk_approval_requests_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
ON DELETE CASCADE; -- 사용자 삭제 시 요청도 삭제

ALTER TABLE `approval_requests`
ADD CONSTRAINT `fk_approval_requests_assigned_admin_id` FOREIGN KEY (`assigned_admin_id`) REFERENCES `users`(`id`);

-- springboottemplate DB의 blacklisted_tokens Table Definition
CREATE TABLE `blacklisted_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `token` VARCHAR(512) NOT NULL UNIQUE, -- 블랙리스트에 추가할 JWT 토큰 (길이 부족 시 TEXT 타입 고려)
    `expires_at` DATETIME NOT NULL, -- 해당 JWT의 원래 만료 시간
    `blacklisted_at` DATETIME DEFAULT CURRENT_TIMESTAMP -- 블랙리스트에 추가된 시간
);

-- springboottemplate DB의 participants Table Definition
CREATE TABLE `participants` (
    `participant_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `participant_name` VARCHAR(50) NOT NULL,
    -- 생년 (SMALLINT: 1900년대부터 현재까지의 연도를 저장)
    `birth_year` SMALLINT NOT NULL,
    -- 생월 (TINYINT: 1부터 12까지의 월(달)을 저장)
    `birth_month` TINYINT NOT NULL CHECK (`birth_month` >= 1 AND `birth_month` <= 12),
    -- 성별 (VARCHAR(10): 'MALE', 'FEMALE', 'OTHER' 등을 지정, CHECK 제약조건으로 유효값 제한)
    `gender` VARCHAR(10) NOT NULL CHECK (`gender` IN ('MALE', 'FEMALE', 'OTHER')),
    `grade` VARCHAR(20) NOT NULL DEFAULT 'NONE',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE `participants`
ADD CONSTRAINT `fk_participants_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
ON DELETE RESTRICT; -- 사용자 삭제 시 참가자는 유지

-- CREATE FULLTEXT INDEX `idx_fulltext_participant_name` ON `participants`(`participant_name`) WITH PARSER ngram;
ALTER TABLE `participants`
ADD FULLTEXT `idx_fulltext_participant_name` (`participant_name`) WITH PARSER ngram;

-- springboottemplate DB의 records Table Definition
CREATE TABLE `records` (
    `record_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `participant_id` BIGINT NOT NULL,
    -- 녹음순서 (1, 2, 3 또는 그 이상)
    `record_sequence` TINYINT NOT NULL,
    -- 실제 파일 시스템에 저장된 음성 파일의 경로
    `file_path` VARCHAR(255) NOT NULL,
    -- 파일의 MIME 타입 (음성 파일 재생에 필요)
    `mime_type` VARCHAR(50) NOT NULL, -- mp4 형식임을 명시 (예: audio/mp4 또는 video/mp4)
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 한 참가자의 녹음 순서는 중복될 수 없도록 UNIQUE 인덱스 추가 (예: participant_id=1, record_sequence=1은 하나만 존재)
    UNIQUE (`participant_id`, `record_sequence`)
);

ALTER TABLE `records`
ADD CONSTRAINT `fk_records_participant_id` FOREIGN KEY (`participant_id`) REFERENCES `participants`(`participant_id`)
ON DELETE CASCADE; -- 참가자 삭제 시 해당 참가자의 모든 녹음 기록도 함께 삭제

-- springboottemplate DB에서 roles, users, approval_requests Table의 필수 데이터 삽입
INSERT INTO `roles` (`rolename`, `description`)
VALUES ('SYSTEM', '시스템 관리자'), ('ADMIN', '관리자'), ('USER', '일반 사용자');

INSERT INTO `users` (`username`, `password`, `displayname`, `email`, `role_id`, `is_deleted`)
VALUES ('system', '$2a$10$8qm3D6XAYYR5OeUgvwRZF.ikCtp/BdM9jnmQGq4NaEEbVzzUF6fLa', '시스템 관리자', 'system@springboottemplate.co.kr', 1, 0);

INSERT INTO `approval_requests` (`user_id`, `is_approved`)
VALUES (1, 1);

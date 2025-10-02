package kr.co.jaehoon.springboottemplate.dto.validation;

// 성별 Enum (`gender` VARCHAR(10) NOT NULL CHECK (`gender` IN ('MALE', 'FEMALE', 'OTHER')))
public enum Gender {
    MALE, FEMALE, OTHER
}

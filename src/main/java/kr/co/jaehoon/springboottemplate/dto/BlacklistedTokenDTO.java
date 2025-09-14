package kr.co.jaehoon.springboottemplate.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@ToString
public class BlacklistedTokenDTO {

    private Long id;
    private String token;

    private LocalDateTime expiresAt;      // DB의 expires_at 컬럼과 매핑
    private LocalDateTime blacklistedAt;  // DB의 blacklisted_at 컬럼과 매핑
}

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
public class UserDTO {

    private Long id;
    private String username;     // 아이디
    private String password;     // 암호화된 비밀번호
    private String displayname;  // 화면에 표시할 이름

    private String email;        // 이메일 주소
    private String reqMessage;   // 요청 메시지
    private String role;         // SYSTEM, ADMIN, USER 등의 권한
    private String adminname;    // 담당 관리자 이름
    private boolean isApproved;  // 로그인 승인여부

    private String activeSessionJti;  // 현재 활성화된 JWT의 Jti(JWT ID)를 저장

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

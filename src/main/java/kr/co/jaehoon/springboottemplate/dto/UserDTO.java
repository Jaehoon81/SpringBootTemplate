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
    private String username;
    private String password;  // 암호화된 비밀번호
    private String displayname;
    private String role;  // ADMIN, USER 등 권한
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

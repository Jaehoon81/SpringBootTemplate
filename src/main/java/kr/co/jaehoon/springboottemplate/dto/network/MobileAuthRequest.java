package kr.co.jaehoon.springboottemplate.dto.network;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "모바일 인증(로그인) 요청 데이터")
@Data
public class MobileAuthRequest {

    @Schema(description = "사용자 아이디", example = "user01")
    private String username;
    @Schema(description = "비밀번호", example = "1234qwer!!")
    private String password;
}

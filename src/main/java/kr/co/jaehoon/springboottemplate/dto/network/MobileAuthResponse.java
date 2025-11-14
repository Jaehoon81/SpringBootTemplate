package kr.co.jaehoon.springboottemplate.dto.network;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

@Schema(description = "모바일 인증(로그인/로그아웃) 응답 데이터")
@Data
@Builder
public class MobileAuthResponse {

    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String accessToken;  // 로그인 시에만 포함
    @Schema(description = "사용자 아이디", example = "user01")
    private String username;
    @Schema(description = "사용자 이름", example = "사용자01")
    private String displayName;
    @Schema(description = "사용자 권한(역할)", example = "USER")
    private String roleName;
    @Schema(description = "응답 메시지", example = "로그인/로그아웃(모바일) 성공")
    private String resMessage;

    /**
     * CustomUserDetails 모델 객체를 MobileAuthResponse로 변환하는 팩토리 메서드
     * @param jwtToken JWT Access Token
     * @param userDetails 변환할 CustomUserDetails 모델 객체
     * @param resMessage 응답 메시지
     * @return MobileAuthResponse 객체
     */
    public static MobileAuthResponse from(String jwtToken, CustomUserDetails userDetails, String resMessage) {
        return MobileAuthResponse.builder()
                .accessToken(jwtToken)
                .username(userDetails.getUsername())
                .displayName(userDetails.getDisplayname())
                .roleName(userDetails.getUser().getRolename())
                .resMessage(resMessage)
                .build();
    }
}

package kr.co.jaehoon.springboottemplate.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@ToString
public class UserDTO {

    private Long id;
    private String username;            // 아이디
    private String password;            // 암호화된 비밀번호
    private String displayname;         // 화면에 표시할 이름
    private String profilePicturePath;  // 프로필 사진(이미지) 경로
    private String email;               // 이메일 주소
//    private String role;                // SYSTEM, ADMIN, USER 등의 권한
    private Long roleId;                // role 대신 roleId로 변경
    private String activeSessionJti;    // 현재 활성화된 JWT의 Jti(JWT ID)를 저장
    private boolean isDeleted;          // 회원탈퇴 여부

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // roles 테이블의 rolename을 가져오기 위한 필드 (DB 컬럼은 아니지만 조인해서 매핑)
    private String rolename;  // JSP, Security에서 사용
    public String getRole() {
        return this.rolename;
    }
    // USER 권한의 계정인 경우, 담당 관리자(ADMIN)의 displayname을 가져오기 위한 필드 (DB 컬럼은 아니지만 조인해서 매핑)
    private String assignedAdminName;
    // 로그인하는 사용자의 승인 요청 정보를 가져오기 위한 필드 (DB 컬럼은 아니지만 조인해서 매핑)
    private ApprovalRequestDTO approvalRequest;
    public boolean isApproved() {
        return (approvalRequest != null && approvalRequest.isApproved());
    }
    // 로그인한 사용자의 참가자 목록 (users-participants 관계)
    private List<ParticipantDTO> participants;
}

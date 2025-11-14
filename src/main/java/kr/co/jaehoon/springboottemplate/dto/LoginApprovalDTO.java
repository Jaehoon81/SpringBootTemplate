package kr.co.jaehoon.springboottemplate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class LoginApprovalDTO {

    private Long id;                                        // SYSTEM or ADMIN 계정의 ID값 (Primary Key)
    private String username;                                // 아이디
    private String displayname;                             // 화면에 표시할 이름
    private String email;                                   // 이메일 주소
    @JsonProperty("reqMessage") private String reqMessage;  // 요청 메시지 (From approval_requests.req_message)

    private String rolename;                                              // 권한(역할) 이름 (From roles.rolename)
    @JsonProperty("assignedAdminId") private Long assignedAdminId;        // 담당 관리자(ADMIN)의 users.id 참조
    @JsonProperty("assignedAdminName") private String assignedAdminName;  // 담당 관리자(ADMIN)의 displayname을 매핑 (조인해서 채움)
}

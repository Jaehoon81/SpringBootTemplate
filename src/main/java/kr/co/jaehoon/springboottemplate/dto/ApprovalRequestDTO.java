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
public class ApprovalRequestDTO {

    private Long requestId;
    private Long userId;
    private String reqMessage;         // 요청 메시지
    private Long assignedAdminId;      // 담당 관리자(ADMIN)의 users.id 참조
    private String assignedAdminName;  // 담당 관리자(ADMIN)의 displayname을 매핑 (조인해서 채움)
    private boolean isApproved;        // 로그인 승인여부

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
}

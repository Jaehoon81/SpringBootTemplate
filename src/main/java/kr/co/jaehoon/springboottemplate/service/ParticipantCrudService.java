package kr.co.jaehoon.springboottemplate.service;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.dto.network.ParticipantListResponse;
import kr.co.jaehoon.springboottemplate.dto.validation.Gender;
import kr.co.jaehoon.springboottemplate.dto.validation.Grade;
import kr.co.jaehoon.springboottemplate.dto.validation.GradeUpdateRequest;
import kr.co.jaehoon.springboottemplate.dto.validation.ParticipantRequest;
import kr.co.jaehoon.springboottemplate.repository.ParticipantRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.ParticipantDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//@Slf4j
public class ParticipantCrudService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    private final ParticipantDAO participantDAO;
//    private final ParticipantRepository participantRepository;
    private final ParticipantService participantService;

    @Transactional
    public ParticipantDTO registerParticipant(Long userId, ParticipantRequest request) throws Exception {
        ParticipantDTO participant = new ParticipantDTO();
        participant.setUserId(userId);  // 현재 로그인한 사용자의 ID를 설정
        participant.setParticipantName(request.getParticipantName());
        participant.setBirthYear(request.getBirthYear());
        participant.setBirthMonth(request.getBirthMonth());
        participant.setGender(request.getGender());  // Enum 값을 직접 설정
        participant.setGrade(request.getGrade());  // Enum 값을 직접 설정

        participantService.saveParticipant(participant);
        return participant;
    }

    // 특정 participantId가 해당 userId의 소유인지 확인
    public boolean isOwner(Long participantId, Long userId) throws Exception {
        Optional<ParticipantDTO> participant = participantService.findByParticipantId(participantId);
        return participant.map(p -> p.getUserId().equals(userId)).orElse(false);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaginatedParticipantList(
            int page, int pageSize, Grade grade, LocalDate startDate, LocalDate endDate, Long currentUserId, String rolename
    ) throws Exception {
        int offset = (page - 1) * pageSize;

        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("limit", pageSize);
        params.put("grade", grade);  // 등급에 따른 필터링을 위해 전달
        params.put("startDate", startDate);  // 시작일에 따른 필터링을 위해 전달
        params.put("endDate", endDate);  // 종료일에 따른 필터링을 위해 전달
        params.put("currentUserId", currentUserId);
        params.put("rolename", rolename);  // 권한(역할)에 따른 필터링을 위해 전달

        List<ParticipantListResponse> participantList = participantService.findPaginatedParticipantList(params);
        int totalCount = participantService.countParticipants(params);
        // listNumber: 리스트 번호(인덱스) 설정
        // 가장 최근 것이 마지막 번호 = 리스트의 총 개수 - (현재 페이지의 첫 번째 ~ 열 번째 인덱스)
        // (예를 들어 총 100개 리스트, 10개씩 페이징, 1 페이지(0 ~ 9 인덱스)의 경우: 100 - 0 = 100, 100 - 1 = 99, 100 - 2 = 98, ...)
        // (실제 화면에 표시될 때는 100, 99, 98, ... 순서로 표시)
        for (int i = 0; i < participantList.size(); i++) {
            participantList.get(i).setListNumber((long) totalCount - (offset + i));
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("participants", participantList);
        resultMap.put("totalCount", totalCount);
        resultMap.put("currentPage", page);
        resultMap.put("pageSize", pageSize);
        resultMap.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

        return resultMap;
    }

//    @Transactional(readOnly = true)
//    public Map<String, Object> getParticipantRecordDetails(Long participantId, Long currentUserId, String rolename) throws Exception {
//        // 보안 로직: 현재 로그인한 사용자가 해당 참가자 정보에 접근할 권한이 있는지 확인
//        // - SYSTEM 권한: 모든 참가자에 접근 가능
//        // - ADMIN 권한: 소속된 USER의 참가자만 접근 가능
//        // - USER 권한: 자신의 참가자만 접근 가능
//        if (rolename.equals("USER")) {
//            // USER 권한은 자신의 참가자만 접근할 수 있으므로 명시적으로 소유자 확인
//            if (!participantService.findByParticipantId(participantId)
//                    .map(p -> p.getUserId().equals(currentUserId)).orElse(false)) {
//                throw new AccessDeniedException("해당 참가자 정보에 접근할 권한이 없습니다.");
//            }
//        } else if (rolename.equals("ADMIN")) {
//            // ADMIN 권한은 소속된 USER의 참가자만 접근할 수 있으므로 USER의 담당 관리자가 자신인지 확인
//            // (페이징 처리된 참가자 목록을 조회(findPaginatedParticipantList) 시
//            // ar.assigned_admin_id = #{currentUserId} 조건으로 참가자를 필터링하기 때문에
//            // 이미 ADMIN 권한이 접근할 수 있는 소속된 USER의 참가자이므로 여기서는 더이상 추가할 로직은 없음)
//        } else {  // rolename.equals("SYSTEM")
//            // SYSTEM 권한의 경우 별도 체크 없이 통과
//        }
//        // 특정 참가자 정보를 조회하여 ParticipantListResponse 모델의 각 필드에 매핑
//        ParticipantListResponse participantInfo = participantService.findByParticipantId(participantId)
//                .map(p -> {
//                    try {
//                        return ParticipantListResponse.builder()
//                                .participantId(p.getParticipantId())
//                                .participantName(p.getParticipantName())
//                                .birthYearMonth(p.getBirthYear() + "/" + String.format("%02d", p.getBirthMonth()))
//                                .genderKor(getGenderKor(p.getGender()))
//                                .grade(p.getGrade())
//                                .assignedAdminName(participantService.findPaginatedParticipantList(
//                                        Map.of("currentUserId", p.getUserId(), "rolename", "USER", "offset", 0, "limit", 1)
//                                ).stream().findFirst().map(ParticipantListResponse::getAssignedAdminName).orElse("-"))
//                                .build();
//                    } catch (Exception e) {
////                        throw new RuntimeException(e);
//                        throw new AccessDeniedException("해당 참가자 정보에 접근할 권한이 없습니다.");
//                    }
//                }).orElseThrow(() -> new IllegalArgumentException("해당 참가자 정보를 찾을 수 없습니다."));
//        // 해당 참가자의 모든 음성녹음 정보를 조회
//        List<ParticipantListResponse.RecordInfo> audioRecordList = participantService.findAllRecordInfoByParticipantId(participantId);
//
//        Map<String, Object> resultMap = new HashMap<>();
//        resultMap.put("participantInfo", participantInfo);
//        resultMap.put("audioRecords", audioRecordList);
//
//        return resultMap;
//    }

    @Transactional(readOnly = true)
    public Map<String, Object> getParticipantRecordDetails(Long participantId, Long currentUserId, String rolename) throws Exception {
        // 1. 특정 참가자 정보를 조회
        Optional<ParticipantDTO> participantOptional = participantService.findByParticipantId(participantId);
        // 해당 참가자가 존재하지 않는 경우
        if (participantOptional.isEmpty()) {
            throw new IllegalArgumentException("ID: '" + participantId + "'에 해당하는 참가자 정보를 찾을 수 없습니다.");
        }
        ParticipantDTO participant = participantOptional.get();
        // 2. 보안 로직: 현재 로그인한 사용자가 해당 참가자 정보에 접근할 권한이 있는지 확인
        // - SYSTEM 권한: 모든 참가자에 접근 가능
        // - ADMIN 권한: 소속된 USER의 참가자만 접근 가능
        // - USER 권한: 자신의 참가자만 접근 가능
        if (rolename.equals("USER")) {
            // USER 권한은 자신의 참가자만 접근할 수 있으므로 명시적으로 소유자 확인
            if (!participant.getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("USER 계정은 자신의 참가자 정보에만 접근할 수 있습니다.");
            }
        } else if (rolename.equals("ADMIN")) {
            // ADMIN 권한은 소속된 USER의 참가자만 접근할 수 있으므로 USER의 담당 관리자가 자신인지 확인
            boolean isAdminManagingUser = participantService.isAdminManagingUser(currentUserId, participant.getUserId());
            if (!isAdminManagingUser) {
                throw new AccessDeniedException("ADMIN 계정은 소속된 USER의 참가자 정보에만 접근할 수 있습니다.");
            }
        } else {  // rolename.equals("SYSTEM")
            // SYSTEM 권한의 경우 별도 체크 없이 통과
        }
        // 3. 해당 참가자 정보를 ParticipantListResponse 모델의 각 필드에 매핑
        ParticipantListResponse participantInfo = ParticipantListResponse.builder()
                .participantId(participant.getParticipantId())
                .participantName(participant.getParticipantName())
                .birthYearMonth(participant.getBirthYear() + "/" + String.format("%02d", participant.getBirthMonth()))
                .genderKor(getGenderKor(participant.getGender()))
                .grade(participant.getGrade())
                .assignedAdminName(participantService.findAdminNameForUser(participant.getUserId()).orElse("-"))
                .build();
        // 4. 해당 참가자의 모든 음성녹음 정보를 조회
        List<ParticipantListResponse.RecordInfo> audioRecordList = participantService.findAllRecordInfoByParticipantId(participantId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("participantInfo", participantInfo);
        resultMap.put("audioRecords", audioRecordList);

        return resultMap;
    }

    private String getGenderKor(Gender gender) {
        if (gender == null) { return "알 수 없음"; }
        return switch (gender) {
            case MALE   -> "남자";
            case FEMALE -> "여자";
            case OTHER  -> "기타";
            default     -> "알 수 없음";
        };
    }

    @Transactional
    public void updateParticipantGrade(GradeUpdateRequest request, Long currentUserId, String rolename) throws Exception {
        // 1. 특정 참가자 정보를 조회
        Optional<ParticipantDTO> participantOptional = participantService.findByParticipantId(request.getParticipantId());
        // 해당 참가자가 존재하지 않는 경우
        if (participantOptional.isEmpty()) {
            throw new IllegalArgumentException("ID: '" + request.getParticipantId() + "'에 해당하는 참가자 정보를 찾을 수 없습니다.");
        }
        ParticipantDTO participant = participantOptional.get();
        // 2. 등급변경 권한 확인: SYSTEM or ADMIN 권한만 등급 변경이 가능하도록 설정 (USER 권한은 등급 변경 불가능)
        if (rolename.equals("USER")) {
            if (!participant.getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("USER 계정은 자신의 참가자 정보에만 접근할 수 있습니다.");
            } else {  // USER 권한은 자신의 참가자 등급 변경이 불가능
                throw new AccessDeniedException("USER 계정은 자신의 참가자 등급을 변경할 수 없습니다.");
            }
        } else if (rolename.equals("ADMIN")) {
            // ADMIN 권한은 소속된 USER의 참가자 등급만 변경 가능
            boolean isAdminManagingUser = participantService.isAdminManagingUser(currentUserId, participant.getUserId());
            if (!isAdminManagingUser) {
                throw new AccessDeniedException("ADMIN 계정은 소속된 USER의 참가자 등급만 변경할 수 있습니다.");
            }
        } else {  // rolename.equals("SYSTEM")
            // SYSTEM 권한의 경우 제한 없이 등급 변경이 가능
        }
        // 3. 해당 참가자의 등급 업데이트를 수행
        participantService.updateParticipantGrade(request.getParticipantId(), request.getNewGrade());
    }
}

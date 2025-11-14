package kr.co.jaehoon.springboottemplate.repository.dao;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.dto.network.ParticipantListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Mapper
public interface ParticipantDAO {

    // 특정 참가자(participantId) 정보를 조회
    public Optional<ParticipantDTO> findByParticipantId(@Param("participantId") Long participantId) throws Exception;

    // 특정 사용자(userId)의 모든 참가자 정보를 조회
    public List<ParticipantDTO> findParticipantsByUserId(@Param("userId") Long userId) throws Exception;

    // 페이징 처리된 참가자 목록을 조회 (필터링 및 페이지네이션 포함)
    public List<ParticipantListResponse> findPaginatedParticipantList(Map<String, Object> params) throws Exception;

    // 페이지네이션 없이 필터링된 모든 참가자 목록을 조회
    public List<ParticipantListResponse> findAllFilteredParticipants(Map<String, Object> params) throws IOException;

    // 조건에 맞는 전체 참가자 수를 조회 (필터링 포함)
    public Integer countParticipants(Map<String, Object> params) throws Exception;

    // 단일 참가자의 모든 음성녹음 정보를 조회 (record_sequence 순서대로)
    public List<ParticipantListResponse.RecordInfo> findAllRecordInfoByParticipantId(@Param("participantId") Long participantId) throws Exception;

    // 단일 참가자의 녹음 일자를 조회 (첫 번째 음성: record_sequence = 1)
    public Optional<String> findFirstRecordDate(@Param("participantId") Long participantId) throws Exception;

    // 해당 ADMIN 계정이 특정 USER를 관리(담당)하는지 확인
    public boolean isAdminManagingUser(@Param("adminUserId") Long adminUserId, @Param("managedUserId") Long managedUserId) throws Exception;

    // 특정 USER를 관리(담당)하는 ADMIN의 이름(displayname)을 조회
    public Optional<String> findAdminNameForUser(@Param("managedUserId") Long managedUserId) throws Exception;
}

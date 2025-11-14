package kr.co.jaehoon.springboottemplate.service;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.dto.network.ParticipantListResponse;
import kr.co.jaehoon.springboottemplate.dto.validation.Grade;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ParticipantService {

    public Optional<ParticipantDTO> findByParticipantId(Long participantId) throws Exception;

    public List<ParticipantDTO> findParticipantsByUserId(Long userId) throws Exception;

    public List<ParticipantListResponse> findPaginatedParticipantList(Map<String, Object> params) throws Exception;

    public List<ParticipantListResponse> findAllFilteredParticipants(Map<String, Object> params) throws IOException;

    public Integer countParticipants(Map<String, Object> params) throws Exception;

    public List<ParticipantListResponse.RecordInfo> findAllRecordInfoByParticipantId(Long participantId) throws Exception;

    public Optional<String> findFirstRecordDate(Long participantId) throws Exception;

    public boolean isAdminManagingUser(Long adminUserId, Long managedUserId) throws Exception;

    public Optional<String> findAdminNameForUser(Long managedUserId) throws Exception;

    public void saveParticipant(ParticipantDTO participant) throws Exception;

    public void updateParticipantGrade(Long participantId, Grade newGrade) throws Exception;
}

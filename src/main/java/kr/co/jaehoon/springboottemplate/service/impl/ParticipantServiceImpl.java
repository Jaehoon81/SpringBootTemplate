package kr.co.jaehoon.springboottemplate.service.impl;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.dto.network.ParticipantListResponse;
import kr.co.jaehoon.springboottemplate.dto.validation.Grade;
import kr.co.jaehoon.springboottemplate.repository.ParticipantRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.ParticipantDAO;
import kr.co.jaehoon.springboottemplate.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantDAO participantDAO;
    private final ParticipantRepository participantRepository;

    @Transactional
    @Override
    public Optional<ParticipantDTO> findByParticipantId(Long participantId) throws Exception {
        return participantDAO.findByParticipantId(participantId);
    }

    @Transactional
    @Override
    public List<ParticipantDTO> findParticipantsByUserId(Long userId) throws Exception {
        return participantDAO.findParticipantsByUserId(userId);
    }

    @Transactional
    @Override
    public List<ParticipantListResponse> findPaginatedParticipantList(Map<String, Object> params) throws Exception {
        return participantDAO.findPaginatedParticipantList(params);
    }

    @Transactional
    @Override
    public List<ParticipantListResponse> findAllFilteredParticipants(Map<String, Object> params) throws IOException {
        return participantDAO.findAllFilteredParticipants(params);
    }

    @Transactional
    @Override
    public Integer countParticipants(Map<String, Object> params) throws Exception {
        return participantDAO.countParticipants(params);
    }

    @Transactional
    @Override
    public List<ParticipantListResponse.RecordInfo> findAllRecordInfoByParticipantId(Long participantId) throws Exception {
        return participantDAO.findAllRecordInfoByParticipantId(participantId);
    }

    @Transactional
    @Override
    public Optional<String> findFirstRecordDate(Long participantId) throws Exception {
        return participantDAO.findFirstRecordDate(participantId);
    }

    @Transactional
    @Override
    public boolean isAdminManagingUser(Long adminUserId, Long managedUserId) throws Exception {
        return participantDAO.isAdminManagingUser(adminUserId, managedUserId);
    }

    @Transactional
    @Override
    public Optional<String> findAdminNameForUser(Long managedUserId) throws Exception {
        return participantDAO.findAdminNameForUser(managedUserId);
    }

    @Transactional
    @Override
    public void saveParticipant(ParticipantDTO participant) throws Exception {
        participantRepository.saveParticipant(participant);
    }

    @Transactional
    @Override
    public void updateParticipantGrade(Long participantId, Grade newGrade) throws Exception {
        participantRepository.updateParticipantGrade(participantId, newGrade);
    }
}

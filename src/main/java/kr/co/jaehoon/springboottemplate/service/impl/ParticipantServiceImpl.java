package kr.co.jaehoon.springboottemplate.service.impl;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.repository.ParticipantRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.ParticipantDAO;
import kr.co.jaehoon.springboottemplate.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantDAO participantDAO;
    private final ParticipantRepository participantRepository;

    @Transactional
    @Override
    public Optional<ParticipantDTO> findByParticipantId(Long participantId) throws IOException {
        return participantDAO.findByParticipantId(participantId);
    }

    @Transactional
    @Override
    public List<ParticipantDTO> findParticipantsByUserId(Long userId) throws Exception {
        return participantDAO.findParticipantsByUserId(userId);
    }

    @Transactional
    @Override
    public void saveParticipant(ParticipantDTO participant) throws Exception {
        participantRepository.saveParticipant(participant);
    }
}

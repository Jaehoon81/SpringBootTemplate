package kr.co.jaehoon.springboottemplate.service.impl;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.repository.ParticipantRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.ParticipantDAO;
import kr.co.jaehoon.springboottemplate.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantDAO participantDAO;
    private final ParticipantRepository participantRepository;

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

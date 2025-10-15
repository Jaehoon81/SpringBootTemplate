package kr.co.jaehoon.springboottemplate.service;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ParticipantService {

    public Optional<ParticipantDTO> findByParticipantId(Long participantId) throws IOException;

    public List<ParticipantDTO> findParticipantsByUserId(Long userId) throws Exception;

    public void saveParticipant(ParticipantDTO participant) throws Exception;
}

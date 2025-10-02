package kr.co.jaehoon.springboottemplate.service;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;

import java.util.List;

public interface ParticipantService {

    public List<ParticipantDTO> findParticipantsByUserId(Long userId) throws Exception;

    public void saveParticipant(ParticipantDTO participant) throws Exception;
}

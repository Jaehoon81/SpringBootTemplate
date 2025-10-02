package kr.co.jaehoon.springboottemplate.service;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.dto.validation.ParticipantRequest;
import kr.co.jaehoon.springboottemplate.repository.ParticipantRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.ParticipantDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        participantService.saveParticipant(participant);
        return participant;
    }
}

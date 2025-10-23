package kr.co.jaehoon.springboottemplate.repository;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.dto.validation.Grade;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ParticipantRepository {

    private final SqlSessionTemplate sqlSession;

    public void saveParticipant(ParticipantDTO participant) throws Exception {
        sqlSession.insert("Participant.saveParticipant", participant);
    }

    public void updateParticipantGrade(Long participantId, Grade newGrade) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("participantId", participantId);
        params.put("newGrade", newGrade);

        sqlSession.update("Participant.updateParticipantGrade", params);
    }
}

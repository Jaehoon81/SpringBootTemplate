package kr.co.jaehoon.springboottemplate.repository;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ParticipantRepository {

    private final SqlSessionTemplate sqlSession;

    public void saveParticipant(ParticipantDTO participant) throws Exception {
        sqlSession.insert("Participant.saveParticipant", participant);
    }
}

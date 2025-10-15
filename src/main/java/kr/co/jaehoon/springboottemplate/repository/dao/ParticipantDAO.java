package kr.co.jaehoon.springboottemplate.repository.dao;

import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Repository
@Mapper
public interface ParticipantDAO {

    // 특정 참가자(participantId) 정보를 조회
    public Optional<ParticipantDTO> findByParticipantId(@Param("participantId") Long participantId) throws IOException;

    // 특정 사용자(userId)의 모든 참가자 정보를 조회
    public List<ParticipantDTO> findParticipantsByUserId(@Param("userId") Long userId) throws Exception;
}

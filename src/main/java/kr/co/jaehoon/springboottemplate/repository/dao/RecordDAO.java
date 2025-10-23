package kr.co.jaehoon.springboottemplate.repository.dao;

import kr.co.jaehoon.springboottemplate.dto.RecordDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Mapper
public interface RecordDAO {

    // record_id로 특정 녹음을 조회
    public Optional<RecordDTO> findByRecordId(@Param("recordId") Long recordId) throws Exception;

    // 특정 참가자의 특정 순서 녹음이 이미 존재하는지 확인
    public Optional<RecordDTO> findByParticipantIdAndSequence(
            @Param("participantId") Long participantId, @Param("recordSequence") Short recordSequence) throws Exception;

    // 특정 참가자의 모든 녹음 목록을 조회
    public List<RecordDTO> findRecordsByParticipantId(@Param("participantId") Long participantId) throws Exception;
}

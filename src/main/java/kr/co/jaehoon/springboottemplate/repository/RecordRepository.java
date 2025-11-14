package kr.co.jaehoon.springboottemplate.repository;

import kr.co.jaehoon.springboottemplate.dto.RecordDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecordRepository {

    private final SqlSessionTemplate sqlSession;

    // 새로운 녹음 정보를 저장
    public void saveRecord(RecordDTO record) throws Exception {
        sqlSession.insert("Record.saveRecord", record);
    }

    // 기존 녹음 정보를 업데이트 (특정 참가자의 특정 순서 녹음)
    public void updateRecord(RecordDTO record) throws Exception {
        sqlSession.update("Record.updateRecord", record);
    }
}

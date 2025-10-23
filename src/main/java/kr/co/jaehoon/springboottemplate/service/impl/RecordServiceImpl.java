package kr.co.jaehoon.springboottemplate.service.impl;

import kr.co.jaehoon.springboottemplate.dto.RecordDTO;
import kr.co.jaehoon.springboottemplate.repository.RecordRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.RecordDAO;
import kr.co.jaehoon.springboottemplate.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordDAO recordDAO;
    private final RecordRepository recordRepository;

    @Transactional
    @Override
    public Optional<RecordDTO> findByRecordId(Long recordId) throws Exception {
        return recordDAO.findByRecordId(recordId);
    }

    @Transactional
    @Override
    public Optional<RecordDTO> findByParticipantIdAndSequence(Long participantId, Short recordSequence) throws Exception {
        return recordDAO.findByParticipantIdAndSequence(participantId, recordSequence);
    }

    @Transactional
    @Override
    public List<RecordDTO> findRecordsByParticipantId(Long participantId) throws Exception {
        return recordDAO.findRecordsByParticipantId(participantId);
    }

    @Transactional
    @Override
    public void saveRecord(RecordDTO record) throws Exception {
        recordRepository.saveRecord(record);
    }

    @Transactional
    @Override
    public void updateRecord(RecordDTO record) throws Exception {
        recordRepository.updateRecord(record);
    }
}

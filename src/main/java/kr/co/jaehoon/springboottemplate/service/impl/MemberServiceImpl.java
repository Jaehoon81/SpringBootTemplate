package kr.co.jaehoon.springboottemplate.service.impl;

import kr.co.jaehoon.springboottemplate.repository.dao.MemberDAO;
import kr.co.jaehoon.springboottemplate.dto.MemberDTO;
import kr.co.jaehoon.springboottemplate.repository.MemberRepository;
import kr.co.jaehoon.springboottemplate.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    // 아래 1번과 2번 중에서 선택
    private final MemberRepository memberRepository;  // 1) Repository 방식
    private final MemberDAO memberDAO;                // 2) DAO 방식

    @Transactional
    @Override
    public List<MemberDTO> findAll() throws Exception {
        return memberRepository.findAll();
    }

    @Transactional
    @Override
    public MemberDTO selectMember(Long id) throws Exception {
        return memberDAO.selectMember(id);
    }
}

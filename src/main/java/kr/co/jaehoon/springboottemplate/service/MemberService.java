package kr.co.jaehoon.springboottemplate.service;

import kr.co.jaehoon.springboottemplate.dto.MemberDTO;

import java.util.List;

public interface MemberService {

    public List<MemberDTO> findAll() throws Exception;

    public MemberDTO selectMember(Long id) throws Exception;
}

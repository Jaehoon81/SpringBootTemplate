package kr.co.jaehoon.springboottemplate.controller;

import kr.co.jaehoon.springboottemplate.dto.MemberDTO;
import kr.co.jaehoon.springboottemplate.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final MemberService memberService;

    @GetMapping("/")  // /member/
    public String memberList(Model model) throws Exception {
        try {
            List<MemberDTO> memberDTOList = memberService.findAll();
            log.debug("MemberDTO_List: {}", memberDTOList.toString());

            model.addAttribute("memberList", memberDTOList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return "member/list";
    }

    @GetMapping  // /member?id=1
    public String memberDetail(@RequestParam("id") Long id, Model model) throws Exception {
        try {
            MemberDTO memberDTO = memberService.selectMember(id);
            log.debug("MemberDTO: {}", memberDTO.toString());

            model.addAttribute("member", memberDTO);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return "member/detail";
    }
}

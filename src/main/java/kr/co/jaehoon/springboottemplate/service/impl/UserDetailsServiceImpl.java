package kr.co.jaehoon.springboottemplate.service.impl;

import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDAO userDAO;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, BadCredentialsException {
        UserDTO user = userDAO.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        // 회원을 탈퇴한 계정은 로그인 불가
        if (user.isDeleted()) {
            throw new BadCredentialsException("탈퇴 처리된 계정입니다.\n담당 관리자에게 문의해주세요.");
        }
        // ADMIN 또는 USER 권한의 사용자에 대한 승인 상태를 확인
        // (권한이 ADMIN or USER이면서 아직 승인되지 않았다면 로그인 거부)
        if (user.getApprovalRequest() != null && !user.isApproved()) {  // user.getApprovalRequest() == null: 승인 요청 자체가 없는 경우
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                throw new BadCredentialsException(
                        "'"+user.getUsername()+"'" + " 계정은 아직 승인되지 않았습니다.\n" +
                        "시스템 관리자에게 문의해주세요."
                );
            } else if ("USER".equalsIgnoreCase(user.getRole())) {
                throw new BadCredentialsException(
                        "'"+user.getUsername()+"'" + " 계정은 아직 승인되지 않았습니다.\n" +
                        "'"+user.getApprovalRequest().getAssignedAdminName()+"'" + "에게 문의해주세요."
                );
            }
        }
        // 방법 1) GrantedAuthority 없이 org.springframework.security.core.userdetails.User 객체를 생성하여 반환
//        return new User(user.getUsername(), user.getPassword(), Collections.emptyList());

        // 방법 2) user.getRole()에 따라 권한을 부여하고, org.springframework.security.core.userdetails.User 객체를 생성하여 반환
//        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
//        return new User(user.getUsername(), user.getPassword(), authorities);

        // 방법 3) CustomUserDetails 객체를 생성하여 반환
        // (CustomUserDetails 내부의 getAuthorities() 메서드에서 user.getRole()을 사용하여
        // Spring Security의 GrantedAuthority를 적절히 생성함)
        return new CustomUserDetails(user);
    }
}

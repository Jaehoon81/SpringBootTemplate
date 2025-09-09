package kr.co.jaehoon.springboottemplate.service.impl;

import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDTO user = userDAO.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        // 방법 1) GrantedAuthority 없이 org.springframework.security.core.userdetails.User 객체를 생성하여 반환
//        return new User(user.getUsername(), user.getPassword(), Collections.emptyList());

        // 방법 2) user.getRole()에 따라 권한을 부여하고, org.springframework.security.core.userdetails.User 객체를 생성하여 반환
//        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
//        return new User(user.getUsername(), user.getPassword(), authorities);

        // 방법 3) CustomUserDetails 객체를 생성하여 반환
        // (CustomUserDetails 내부의 getAuthorities() 메서드에서 user.getRole()을 사용하여 Spring Security의 GrantedAuthority를 적절히 생성함)
        return new CustomUserDetails(user);
    }
}

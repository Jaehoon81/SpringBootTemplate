package kr.co.jaehoon.springboottemplate.service.impl;

import kr.co.jaehoon.springboottemplate.dto.RegistrationRequest;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import kr.co.jaehoon.springboottemplate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserDTO findByUsername(String username) throws UsernameNotFoundException {
        return userDAO.findByUsername(username);
    }

    @Transactional
    @Override
    public void save(RegistrationRequest validUser) throws Exception {
        UserDTO newUser = new UserDTO();
        newUser.setUsername(validUser.getUsername());
        newUser.setPassword(passwordEncoder.encode(validUser.getPassword()));
        newUser.setDisplayname((validUser.getDisplayname() != null) ? validUser.getDisplayname() : "");
        newUser.setRole(validUser.getRole());

        userRepository.save(newUser);
    }
}

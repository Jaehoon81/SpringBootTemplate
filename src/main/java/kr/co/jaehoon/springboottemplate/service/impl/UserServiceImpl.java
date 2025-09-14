package kr.co.jaehoon.springboottemplate.service.impl;

import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.LoginApprovalDTO;
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

import java.util.List;
import java.util.Map;

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
    public UserDTO findByDisplayname(String displayname) throws Exception {
        return userDAO.findByDisplayname(displayname);
    }

    @Transactional
    @Override
    public List<String> findAdminNames() throws Exception {
        return userDAO.findAdminNames();
    }

    @Transactional
    @Override
    public Integer countAdminByDisplayname(String adminname) throws Exception {
        return userDAO.countAdminByDisplayname(adminname);
    }

//    @Transactional
//    @Override
//    public List<Map<String, Object>> findPendingAdmins() throws Exception {
//        return userDAO.findPendingAdmins();
//    }
    @Transactional
    @Override
    public List<LoginApprovalDTO> findPendingAdmins() throws Exception {
        return userDAO.findPendingAdmins();
    }

    @Transactional
    @Override
    public List<LoginApprovalDTO> findPendingUsersByAdminname(String adminname) throws Exception {
        return userDAO.findPendingUsersByAdminname(adminname);
    }

    @Transactional
    @Override
    public void saveUser(@Valid RegistrationRequest validUser) throws Exception {
        UserDTO newUser = new UserDTO();
        newUser.setUsername(validUser.getUsername());
        newUser.setPassword(passwordEncoder.encode(validUser.getPassword()));
        newUser.setDisplayname((validUser.getDisplayname() != null) ? validUser.getDisplayname() : "");
        newUser.setEmail(validUser.getEmail());
        newUser.setReqMessage(validUser.getReqMessage());
        newUser.setRole(validUser.getRole());
        newUser.setAdminname(validUser.getAdminname());

        userRepository.saveUser(newUser);
    }

    @Transactional
    @Override
    public void updateApprovalStatus(Long id, boolean isApproved) throws Exception {
        userRepository.updateApprovalStatus(id, isApproved);
    }

    @Transactional
    @Override
    public void updateActiveSessionJti(Long id, String jti) throws Exception {
        userRepository.updateActiveSessionJti(id, jti);
    }
}

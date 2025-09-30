package kr.co.jaehoon.springboottemplate.service.impl;

import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.*;
import kr.co.jaehoon.springboottemplate.dto.validation.*;
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
    public UserDTO findUserById(Long id) throws Exception {
        return userDAO.findUserById(id);
    }

    @Transactional
    @Override
    public UserDTO findUserByDisplaynameAndEmail(String displayname, String email) throws IllegalArgumentException {
        return userDAO.findUserByDisplaynameAndEmail(displayname, email);
    }

    @Transactional
    @Override
    public Long findRoleIdByRolename(String rolename) throws Exception {
        return userDAO.findRoleIdByRolename(rolename);
    }

    @Transactional
    @Override
    public Long findAdminIdByDisplayname(String adminname) throws Exception {
        return userDAO.findAdminIdByDisplayname(adminname);
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
    public List<LoginApprovalDTO> findPendingUsersByAdminName(Long adminId) throws Exception {
        return userDAO.findPendingUsersByAdminName(adminId);
    }

    @Transactional
    @Override
    public void saveUser(UserDTO user) throws Exception {
        userRepository.saveUser(user);
    }

    @Transactional
    @Override
    public void updateUser(UserUpdateRequest request) throws Exception {
        userRepository.updateUser(request);
    }

    @Transactional
    @Override
    public void updatePassword(Long id, String password) throws Exception {
        userRepository.updatePassword(id, password);
    }

    @Transactional
    @Override
    public void updateUserDeleteStatus(Long id, boolean isDeleted) throws Exception {
        userRepository.updateUserDeleteStatus(id, isDeleted);
    }

    @Transactional
    @Override
    public Integer deleteUser(Long id) throws Exception {
        return userRepository.deleteUser(id);
    }

    @Transactional
    @Override
    public void saveApprovalRequest(ApprovalRequestDTO request) throws Exception {
        userRepository.saveApprovalRequest(request);
    }

    @Transactional
    @Override
    public void updateApprovalStatus(Long userId, boolean isApproved) throws Exception {
        userRepository.updateApprovalStatus(userId, isApproved);
    }

    @Transactional
    @Override
    public void updateActiveSessionJti(Long id, String jti) throws Exception {
        userRepository.updateActiveSessionJti(id, jti);
    }

    @Transactional
    @Override
    public void updateProfilePicturePath(Long id, String path) throws Exception {
        userRepository.updateProfilePicturePath(id, path);
    }
}

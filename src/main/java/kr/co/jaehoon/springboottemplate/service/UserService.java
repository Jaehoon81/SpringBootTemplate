package kr.co.jaehoon.springboottemplate.service;

import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.ApprovalRequestDTO;
import kr.co.jaehoon.springboottemplate.dto.LoginApprovalDTO;
import kr.co.jaehoon.springboottemplate.dto.RegistrationRequest;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;

public interface UserService {

    public UserDTO findByUsername(String username) throws UsernameNotFoundException;

    public UserDTO findByDisplayname(String displayname) throws Exception;

    public Long findRoleIdByRolename(String rolename) throws Exception;

    public Long findAdminIdByDisplayname(String adminname) throws Exception;

    public List<String> findAdminNames() throws Exception;

    public Integer countAdminByDisplayname(String adminname) throws Exception;

//    public List<Map<String, Object>> findPendingAdmins() throws Exception;
    public List<LoginApprovalDTO> findPendingAdmins() throws Exception;

    public List<LoginApprovalDTO> findPendingUsersByAdminName(Long adminId) throws Exception;

    public void saveUser(UserDTO user) throws Exception;

    public Integer deleteUser(Long id) throws Exception;

    public void saveApprovalRequest(ApprovalRequestDTO request) throws Exception;

    public void updateApprovalStatus(Long userId, boolean isApproved) throws Exception;

    public void updateProfilePicturePath(Long userId, String path) throws Exception;

    public void updateActiveSessionJti(Long id, String jti) throws Exception;
}

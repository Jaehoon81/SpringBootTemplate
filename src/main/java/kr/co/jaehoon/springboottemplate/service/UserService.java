package kr.co.jaehoon.springboottemplate.service;

import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.*;
import kr.co.jaehoon.springboottemplate.dto.validation.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;

public interface UserService {

    public UserDTO findByUsername(String username) throws UsernameNotFoundException;

    public UserDTO findByDisplayname(String displayname) throws Exception;

    public UserDTO findUserById(Long id) throws Exception;

    public UserDTO findUserByDisplaynameAndEmail(String displayname, String email) throws IllegalArgumentException;

    public Long findRoleIdByRolename(String rolename) throws Exception;

    public Long findAdminIdByDisplayname(String adminname) throws Exception;

    public List<String> findAdminNames() throws Exception;

    public Integer countAdminByDisplayname(String adminname) throws Exception;

//    public List<Map<String, Object>> findPendingAdmins() throws Exception;
    public List<LoginApprovalDTO> findPendingAdmins() throws Exception;

    public List<LoginApprovalDTO> findPendingUsersByAdminName(Long adminId) throws Exception;

    public void saveUser(UserDTO user) throws Exception;

    public void updateUser(UserUpdateRequest request) throws Exception;

    public void updatePassword(Long id, String password) throws Exception;

    public void updateUserDeleteStatus(Long id, boolean isDeleted) throws Exception;

    public Integer deleteUser(Long id) throws Exception;

    public void saveApprovalRequest(ApprovalRequestDTO request) throws Exception;

    public void updateApprovalStatus(Long userId, boolean isApproved) throws Exception;

    public void updateActiveSessionJti(Long id, String jti) throws Exception;

    public void updateProfilePicturePath(Long id, String path) throws Exception;
}

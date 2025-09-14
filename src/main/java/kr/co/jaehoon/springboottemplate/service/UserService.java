package kr.co.jaehoon.springboottemplate.service;

import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.LoginApprovalDTO;
import kr.co.jaehoon.springboottemplate.dto.RegistrationRequest;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;

public interface UserService {

    public UserDTO findByUsername(String username) throws UsernameNotFoundException;

    public UserDTO findByDisplayname(String displayname) throws Exception;

    public List<String> findAdminNames() throws Exception;

    public Integer countAdminByDisplayname(String adminname) throws Exception;

//    public List<Map<String, Object>> findPendingAdmins() throws Exception;
    public List<LoginApprovalDTO> findPendingAdmins() throws Exception;

    public List<LoginApprovalDTO> findPendingUsersByAdminname(String adminname) throws Exception;

    public void saveUser(@Valid RegistrationRequest validUser) throws Exception;

    public void updateApprovalStatus(Long id, boolean isApproved) throws Exception;

    public void updateActiveSessionJti(Long id, String jti) throws Exception;
}

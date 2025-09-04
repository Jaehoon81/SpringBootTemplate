package kr.co.jaehoon.springboottemplate.service;

import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.RegistrationRequest;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {

    public UserDTO findByUsername(@Param("username") String username) throws UsernameNotFoundException;

    public void save(@Valid RegistrationRequest validUser) throws Exception;
}

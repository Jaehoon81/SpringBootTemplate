package kr.co.jaehoon.springboottemplate.repository.dao;

import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface UserDAO {

    public UserDTO findByUsername(@Param("username") String username) throws UsernameNotFoundException;
}

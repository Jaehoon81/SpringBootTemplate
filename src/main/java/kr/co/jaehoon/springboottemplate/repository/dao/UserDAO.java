package kr.co.jaehoon.springboottemplate.repository.dao;

import kr.co.jaehoon.springboottemplate.dto.LoginApprovalDTO;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface UserDAO {

    public UserDTO findByUsername(@Param("username") String username) throws UsernameNotFoundException;

    public UserDTO findByDisplayname(@Param("displayname") String displayname) throws Exception;

    // 사용자 정보를 조회 (업데이트 시 현재 정보를 불러오기 위한 용도)
    public UserDTO findUserById(@Param("id") Long id) throws Exception;

    // displayname과 email로 특정 사용자를 조회
    public UserDTO findUserByDisplaynameAndEmail(
            @Param("displayname") String displayname, @Param("email") String email) throws IllegalArgumentException;

    // 권한(역할) 이름(rolename)을 통해 role_id를 조회
    public Long findRoleIdByRolename(@Param("rolename") String rolename) throws Exception;

    // displayname을 기준으로 담당 관리자(ADMIN)의 user_id를 조회
    public Long findAdminIdByDisplayname(@Param("adminname") String adminname) throws Exception;

    // ADMIN 권한을 가진 사용자들의 displayname 목록을 조회
    public List<String> findAdminNames() throws Exception;

    // 특정 이름(displayname)의 ADMIN 권한 사용자가 존재하는지 확인
    public Integer countAdminByDisplayname(@Param("adminname") String adminname) throws Exception;

    // 승인 대기 중인 ADMIN 계정의 목록을 조회 (SYSTEM 페이지용)
//    public List<Map<String, Object>> findPendingAdmins() throws Exception;
    public List<LoginApprovalDTO> findPendingAdmins() throws Exception;

    // 특정 관리자가 담당하는 승인 대기 중인 USER 계정의 목록을 조회 (ADMIN 페이지용)
    public List<LoginApprovalDTO> findPendingUsersByAdminName(@Param("adminId") Long adminId) throws Exception;
}

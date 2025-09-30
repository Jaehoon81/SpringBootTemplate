package kr.co.jaehoon.springboottemplate.repository;

import kr.co.jaehoon.springboottemplate.dto.ApprovalRequestDTO;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.dto.validation.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SqlSessionTemplate sqlSession;

    public void saveUser(UserDTO user) throws Exception {
        sqlSession.insert("User.saveUser", user);
    }

    // 마이 페이지 메뉴 화면에서 사용자 정보를 업데이트 (내 정보 수정 기능)
    public void updateUser(UserUpdateRequest request) throws Exception {
        sqlSession.update("User.updateUser", request);
    }

    // 아이디/비밀번호 찾기 진행 시 사용자 비밀번호(랜덤)를 업데이트
    public void updatePassword(Long id, String password) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("password", password);

        sqlSession.update("User.updatePassword", params);
    }

    // 마이 페이지 메뉴 화면에서 사용자 탈퇴 상태를 업데이트 (USER 계정만 가능)
    public void updateUserDeleteStatus(Long id, boolean isDeleted) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("isDeleted", isDeleted);

        sqlSession.update("User.updateUserDeleteStatus", params);
    }

    public Integer deleteUser(Long id) throws Exception {
        return sqlSession.delete("User.deleteUser", id);
    }

    // ADMIN, USER 계정의 승인 요청 정보를 저장
    public void saveApprovalRequest(ApprovalRequestDTO request) throws Exception {
        sqlSession.insert("User.saveApprovalRequest", request);
    }

    // ADMIN, USER 계정의 승인 상태를 업데이트
    public void updateApprovalStatus(Long userId, boolean isApproved) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("isApproved", isApproved);

        sqlSession.update("User.updateApprovalStatus", params);
    }

    // USER 계정의 active_session_jti 컬럼을 업데이트
    public void updateActiveSessionJti(Long id, String jti) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("jti", jti);

        sqlSession.update("User.updateActiveSessionJti", params);
    }

    // 해당 계정의 프로필 사진(이미지) 경로를 업데이트
    public void updateProfilePicturePath(Long id, String path) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("path", path);

        sqlSession.update("User.updateProfilePicturePath", params);
    }
}

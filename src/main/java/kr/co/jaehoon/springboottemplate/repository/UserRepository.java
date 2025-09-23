package kr.co.jaehoon.springboottemplate.repository;

import kr.co.jaehoon.springboottemplate.dto.ApprovalRequestDTO;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
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

    // 해당 계정의 프로필 사진(이미지) 경로를 업데이트
    public void updateProfilePicturePath(Long userId, String path) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", userId);
        params.put("path", path);

        sqlSession.update("User.updateProfilePicturePath", params);
    }

    // USER 계정의 active_session_jti 컬럼을 업데이트
    public void updateActiveSessionJti(Long id, String jti) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("jti", jti);

        sqlSession.update("User.updateActiveSessionJti", params);
    }
}

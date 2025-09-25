package kr.co.jaehoon.springboottemplate.repository;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BlacklistedTokenRepository {

    private final SqlSessionTemplate sqlSession;

    /**
     * 블랙리스트에 토큰을 추가
     * @param token 블랙리스트에 추가할 JWT 토큰 String
     * @param expiresAt 해당 토큰의 원래 만료 시간 (LocalDateTime)
     */
    public void addToken(String token, LocalDateTime expiresAt) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("token", token);
        params.put("expiresAt", expiresAt);

        sqlSession.insert("BlacklistedToken.addToken", params);
    }

    /**
     * 만료된 블랙리스트 토큰을 DB에서 삭제
     */
    public void deleteExpiredTokens() throws Exception {
        sqlSession.delete("BlacklistedToken.deleteExpiredTokens");
    }
}

package kr.co.jaehoon.springboottemplate.repository.dao;

import kr.co.jaehoon.springboottemplate.dto.BlacklistedTokenDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface BlacklistedTokenDAO {

    /**
     * 특정 토큰이 블랙리스트에 있는지, 그리고 아직 만료되지 않았는지 확인
     * @param token 확인할 JWT 토큰 String
     * @return 블랙리스트에 있고 유효 기간이 남아있는 토큰 정보 (없으면 null)
     */
    public BlacklistedTokenDTO findByTokenAndNotExpired(@Param("token") String token) throws Exception;
}

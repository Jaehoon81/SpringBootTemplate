package kr.co.jaehoon.springboottemplate.repository;

import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SqlSessionTemplate sqlSession;

    public void save(UserDTO user) throws Exception {
        sqlSession.insert("User.save", user);
    }
}

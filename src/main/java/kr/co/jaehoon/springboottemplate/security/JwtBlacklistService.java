package kr.co.jaehoon.springboottemplate.security;

import kr.co.jaehoon.springboottemplate.dto.BlacklistedTokenDTO;
import kr.co.jaehoon.springboottemplate.repository.BlacklistedTokenRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.BlacklistedTokenDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
//@Slf4j
public class JwtBlacklistService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final BlacklistedTokenDAO blacklistedTokenDAO;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    // 토큰만료 시간을 기록하여 해당 시간 이후에는 블랙리스트에서 삭제 (실제 운영 환경에서는 Redis 등 영속적인 저장소를 고려)
    private final Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 토큰을 블랙리스트에 추가 (간단한 인메모리 캐시를 사용)
     * @param token 블랙리스트에 추가할 JWT
     * @param expiryTimeMillis Long 객체 형태의 토큰만료 시간
     */
    public void addTokenToBlacklist(String token, long expiryTimeMillis) {
        blacklistedTokens.add(token);
        long delay = expiryTimeMillis - Instant.now().toEpochMilli();
        if (delay > 0) {
            // 토큰만료 시간 이후에 블랙리스트에서 삭제되도록 스케줄링
            scheduler.schedule(() -> removeTokenFromBlacklist(token), delay, TimeUnit.MILLISECONDS);
        } else {
            // 이미 만료된 토큰인 경우 즉시 삭제
            removeTokenFromBlacklist(token);
        }
    }

//    public boolean isTokenBlacklisted(String token) {
//        return blacklistedTokens.contains(token);
//    }

    /**
     * 토큰을 블랙리스트에서 삭제
     * @param token 삭제할 JWT
     */
    public void removeTokenFromBlacklist(String token) {
        blacklistedTokens.remove(token);
    }

    /**
     * 토큰을 블랙리스트에 추가 (MySQL DB 기반 영속적인 방식)
     * @param token 블랙리스트에 추가할 JWT
     * @param expiration Date 객체 형태의 토큰만료 시간
     */
    @Transactional
    public void addTokenToBlacklist(String token, Date expiration)/* throws Exception*/ {
        // Date를 LocalDateTime으로 변환
        LocalDateTime expiresAt = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        try {
            blacklistedTokenRepository.addToken(token, expiresAt);
        } catch (Exception e) {
            // 이미 블랙리스트에 추가된 토큰이거나 DB 제약 조건 위배 시 발생 (UNIQUE KEY on token)
            // 경고 로그를 남기거나, 이미 처리되었으므로 무시할 수 있음
            log.warn("Failed to add token to blacklist or token already exists: {}", e.getMessage());
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token 확인할 JWT
     * @return 블랙리스트에 있고 아직 만료되지 않았으면 true, 아니면 false
     */
    @Transactional
    public boolean isTokenBlacklisted(String token)/* throws Exception*/ {
        boolean isTokenInBlacklist = false;
        try {
            // DB에서 토큰을 찾아오고, expires_at이 현재 시간보다 큰 경우만 유효하게 간주
            isTokenInBlacklist = blacklistedTokenDAO.findByTokenAndNotExpired(token) != null;
        } catch (Exception e) {
            // 토큰이 블랙리스트에 있는지 확인하는 중 오류 발생
            // 경고 로그를 남기거나, 이미 처리되었으므로 무시할 수 있음
            log.warn("Error checking if JWT token is in blacklist: {}", e.getMessage());
        }
        return isTokenInBlacklist;
    }
}

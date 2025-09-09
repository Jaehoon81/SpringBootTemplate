package kr.co.jaehoon.springboottemplate.scheduler;

import kr.co.jaehoon.springboottemplate.repository.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
//@Slf4j
public class BlacklistCleanupScheduler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    /**
     * 매일 자정(0시 0분 0초)에 만료된 블랙리스트 토큰을 DB에서 삭제
     * - cron 표현식 예시: "0 0 0 * * *" = 매일 0시 0분 0초
     *                   "0 * * * * *" = 매분 0초 (테스트용)
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정 실행
    public void cleanupExpiredBlacklistedTokens() throws Exception {
        log.info("Scheduled task: Cleaning up expired blacklisted tokens...");

        blacklistedTokenRepository.deleteExpiredTokens();
        log.debug("Cleanup of expired blacklisted tokens completed.");
    }
}

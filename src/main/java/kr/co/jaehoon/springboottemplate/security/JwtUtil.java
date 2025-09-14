package kr.co.jaehoon.springboottemplate.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration.web}")  // 1시간 (밀리초)
    private long WEB_EXPIRATION_TIME;

    @Value("${jwt.expiration.mobile}")  // 10분 (밀리초)
    private long MOBILE_EXPIRATION_TIME;

    public long getWebExpirationTime() {
        return WEB_EXPIRATION_TIME;
    }

    public long getMobileExpirationTime() {
        return MOBILE_EXPIRATION_TIME;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractJti(String token) {
        return extractClaim(token, claims -> claims.get("jti", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // 토큰 파싱 중 발생하는 예외(ExpiredJwtException, SignatureException 등)는 JwtRequestFilter에서 try-catch로 검증 및 확인
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    // JwtUtil 외부에서 처리하므로 더이상 사용하지 않음
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username, boolean isMobile) {
        long expirationTime = (isMobile) ? MOBILE_EXPIRATION_TIME : WEB_EXPIRATION_TIME;
        return generateToken(username, expirationTime);
    }

    public String generateToken(String username, long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        String jti = UUID.randomUUID().toString();  // 고유한 JTI 생성
        claims.put("jti", jti);  // JTI 클레임 추가

        return createToken(claims, username, expirationTime);
    }

    private String createToken(Map<String, Object> claims, String username, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, CustomUserDetails userDetails) {
        final String username = extractUsername(token);
        final Date expiration = extractExpiration(token);  // 만료 시간을 추출

        // 토큰의 유저네임과 CustomUserDetails의 유저네임이 일치하는지 확인
        // (만료여부 검증(isTokenExpired())은 JwtRequestFilter에서 호출되는 validateToken에서 처리)
        // (JTI 검증은 JwtRequestFilter에서 수행)
        return (username.equals(userDetails.getUsername()) && !expiration.before(new Date()));
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

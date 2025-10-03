package kr.co.jaehoon.springboottemplate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.dto.network.BasicResponse;
import kr.co.jaehoon.springboottemplate.dto.network.ErrorResponse;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import kr.co.jaehoon.springboottemplate.service.UserService;
import kr.co.jaehoon.springboottemplate.service.impl.UserDetailsServiceImpl;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
//@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;

    private final UserDAO userDAO;
    private final UserRepository userRepository;
//    private final UserService userService;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws ServletException, IOException {
        // 1. 요청이 API 엔드포인트인지 판단 (/api/ 로 시작하는 URI)
        boolean isApiRequest = request.getRequestURI().startsWith("/api/");
        // 2. 클라이언트의 Accept 헤더를 명시적으로 검사하여 JSON을 원하는지 확인
        String acceptHeader = request.getHeader("Accept");
        boolean expectsJsonExplicitly = (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));
        // 3. AJAX 요청인지 확인 (웹에서 AJAX 호출 시 사용하는 X-Requested-With 헤더를 통한 판단)
        boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        // JSON 응답을 보내야 하는 조건:
        // - 명백하게 API 요청인 경우 (경로 기준)
        // - or 클라이언트가 명시적으로 JSON을 요청한 경우
        // - or 일반적인 AJAX 요청인 경우 (일반적으로 JSON 응답을 기대)
        boolean shouldSendJson = isApiRequest || expectsJsonExplicitly || isAjaxRequest;
        boolean isWebBrowser = !expectsJsonExplicitly && isAjaxRequest;

        String jwt = null;
        String jti = null;
        String username = null;

        // 1. HTTP Authorization 헤더에서 JWT 추출 (모바일 또는 웹의 AJAX에서 명시적으로 보낸 경우)
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
//            try {
//                username = jwtUtil.extractUsername(jwt);
//            } catch (Exception e) {
//                log.warn("JWT token extraction failed: {}", e.getMessage());
//                // 토큰 추출 실패 시 다음 필터로 진행하지 않거나 401 응답 처리
////                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
////                return;
//            }
        }
        // 2. HTTP Authorization 헤더에 JWT가 없으면 쿠키에서 JWT 추출 (웹 브라우저의 자동 쿠키 전송)
        if (jwt == null && request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        // 3. JWT가 존재하면 검증 시작
        if (jwt != null) {
            // 블랙리스트에 있는 토큰인지 확인 (모바일/웹 토큰 모두 해당)
            if (jwtBlacklistService.isTokenBlacklisted(jwt)) {
                log.warn("Attempted to use a blacklisted JWT token: {}", jwt);
                // 토큰이 블랙리스트에 있는 경우의 인증 실패 처리
                // 방법 1) 401 HTML 응답을 반환
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 무효화되었습니다.\n다시 로그인해주세요.");
//                return;
                // 방법 2) 401 HTML 응답(response.sendError()) 대신, JwtAuthenticationEntryPoint로 예외를 전달
//                throw new BadCredentialsException("토큰이 무효화되었습니다.\n다시 로그인해주세요.");

                // 방법 3) 직접 401 JSON 응답을 작성하여 전송
                // (ExceptionTranslationFilter가 JwtRequestFilter에서 발생한 AuthenticationException을 예상대로 Catch하지 못하는 상황)
                create401JsonResponse(request, response, shouldSendJson, isWebBrowser,
                        "블랙리스트에 등록된 JWT 토큰입니다.", "토큰이 무효화되었습니다.\n다시 로그인해주세요.");
                return;  // 필터 체인 진행을 중단
            }

            String errorMessage = "";
            String exceptionMessage = "";
            try {
                username = jwtUtil.extractUsername(jwt);
                jti = jwtUtil.extractJti(jwt);  // 토큰에서 JTI 추출
            } catch (ExpiredJwtException e) {
                username = e.getClaims().getSubject();  // 만료된 토큰에서도 subject는 추출

                errorMessage = "사용자의 JWT 토큰이 만료되었습니다.";
                exceptionMessage = e.getMessage();
                log.warn("JWT token has expired for user: {}", (e.getClaims() != null) ? e.getClaims().getSubject() : "unknown user");
                // 만료된 토큰인 경우, 유저네임은 추출 가능하나 유효성 검사에서 실패
            } catch (SignatureException e) {
                errorMessage = "JWT 토큰 서명이 유효하지 않습니다.";
                exceptionMessage = e.getMessage();
                log.warn("JWT token signature is invalid: {}", exceptionMessage);
                // 유효하지 않은 서명인 경우, username을 null로 유지하여 인증 실패로 처리
            } catch (MalformedJwtException e) {
                errorMessage = "손상됐거나 잘못된 형식의 JWT 토큰입니다.";
                exceptionMessage = e.getMessage();
                log.warn("Malformed JWT token: {}", exceptionMessage);
                // 손상된 토큰인 경우, username을 null로 유지하여 인증 실패로 처리
            } catch (Exception e) {
                errorMessage = "JWT 토큰이 잘못됐거나 가져올 수 없습니다.";
                exceptionMessage = e.getMessage();
                log.warn("Unable to get JWT token or invalid token: {}", exceptionMessage);
                // 그 외 모든 JWT 관련 예외 처리 (유효하지 않은 토큰)
            }
            if (!errorMessage.isEmpty() && !exceptionMessage.isEmpty()) {
                // SecurityContext에 인증 정보를 설정하지 않고, JwtAuthenticationEntryPoint에서 401을 처리
                create401JsonResponse(request, response, shouldSendJson, isWebBrowser, errorMessage, exceptionMessage);
                return;  // 필터 체인 진행을 중단
            }
        }
        // 4. username이 있고, 아직 SecurityContext에 인증 정보가 없는 경우에만 인증 시도
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(username);
            } catch (Exception e) {
                log.warn("User not found or other UserDetails error during loadUserByUsername: {}", e.getMessage());
                // 사용자 정보를 불러오는데 실패하면 인증 실패 처리
                // 방법 1) 401 HTML 응답을 반환
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "사용자 정보를 불러올 수 없습니다.");
//                return;
                // 방법 2) 401 HTML 응답(response.sendError()) 대신, JwtAuthenticationEntryPoint로 예외를 전달
//                throw new BadCredentialsException("사용자 정보를 불러올 수 없습니다.");

                // 방법 3) BadCredentialsException을 발생시키는 대신, 이 부분을 건너뛰어
                // SecurityContextHolder.getContext().getAuthentication()이 null 상태로 다음 필터로 진행되고
                // 결국 JwtAuthenticationEntryPoint가 처리하도록 유도함
            }
            // 토큰 유효성 최종 검증 (블랙리스트 검증은 이미 위에서 수행됨)
            if (userDetails != null) {
                // active_session_jti 값을 확인하기 위해 DB에서 사용자 정보 가져오기
                UserDTO user = userDAO.findByUsername(username);
                // 모바일 중복 로그인 방지 (JTI 일치여부 확인)
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {  // 모바일 토큰의 경우 JTI 검증
                    if (user != null && jti != null && user.getActiveSessionJti() != null && !user.getActiveSessionJti().equals(jti)) {
                        log.warn("JTI mismatch detected for user: {} - Token JTI: {}, DB JTI: {}", username, jti, user.getActiveSessionJti());

                        // DB에 저장된 JTI 값과 현재 토큰의 JTI가 일치하지 않음
                        create401JsonResponse(request, response, shouldSendJson, isWebBrowser,
                                "사용자에 대한 JTI가 불일치합니다.", "다른 기기에서 로그인했거나 세션이 만료되었을 수 있습니다.\n다시 로그인해주세요.");
                        return;  // 필터 체인 진행을 중단
                    } else if (user != null && jti != null && user.getActiveSessionJti() == null) {
                        log.warn("JTI null in DB for user: {} - Token JTI: {}", username, jti);

                        // DB에 저장된 JTI 값이 없는데, 현재 토큰에는 JTI가 있음 (모바일 로그아웃 상태에서 이전 토큰 사용 등)
                        create401JsonResponse(request, response, shouldSendJson, isWebBrowser,
                                "사용자 계정의 JTI가 null 값입니다.", "세션이 만료되었습니다.\n다시 로그인해주세요.");
                        return;  // 필터 체인 진행을 중단
                    }
                }
                // 토큰 만료여부, 서명 등의 유효성 검증
                if (jwtUtil.validateToken(jwt, (CustomUserDetails) userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
                    log.warn("JWT token validation failed for user: {}", username);
                    // validateToken에서 실패한 경우 (ExpiredJwtException 때문에 false를 반환)
                    // 인증 정보가 설정되지 않고, 다음 필터로 넘어가서 최종적으로 JwtAuthenticationEntryPoint가 처리
                }
            } else {
                log.warn("UserDetails is null after loadUserByUsername for: {}", username);
                // 사용자 정보 자체를 불러오는데 실패한 경우 (UserDetails가 null을 반환)
                // 인증 정보가 설정되지 않고, 다음 필터로 넘어가서 최종적으로 JwtAuthenticationEntryPoint가 처리
            }
        }
        // 다음 필터 체인으로 진행
        chain.doFilter(request, response);
    }

    private void create401JsonResponse(
            HttpServletRequest request, HttpServletResponse response, boolean shouldSendJson, boolean isWebBrowser,
            String errorMessage, String exceptionMessage
    ) throws ServletException, IOException {
        if (shouldSendJson == true) {
            // JSON 응답을 기대하는 클라이언트 (모바일 앱, API 클라이언트 등)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);    // 401 Unauthorized
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // Content-Type 설정
            response.setCharacterEncoding("UTF-8");                     // 한글 깨짐 방지

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", errorMessage);  // 상세 메시지
            errorDetails.put("exceptionMessage", exceptionMessage);  // 예외 메시지 (개발 및 디버그 용도로 사용)

            if (isWebBrowser == true) {  // expectsJsonExplicitly == false && isAjaxRequest == true
                // 웹 브라우저용 JSON 응답 작성
                objectMapper.writeValue(response.getWriter(), errorDetails);
            } else {  // expectsJsonExplicitly == true && isAjaxRequest == false
                // 모바일 앱용 BasicResponse 및 ErrorResponse로 JSON 응답 작성
                objectMapper.writeValue(response.getWriter(), BasicResponse.failure(
                        ErrorResponse.from(
                                (int) errorDetails.get("status"),
                                (String) errorDetails.get("error"),
                                (String) errorDetails.get("message"),
                                (String) errorDetails.get("exceptionMessage"))
                ));
            }
        } else {
            // 웹 브라우저에서 직접 접근(페이지 요청)인 경우:
            // 1. 401 HTML 응답(response.sendError()) 대신, 응답 상태 코드를 401으로 설정하고 /error 경로로 포워드
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // 2. CustomErrorController에서 사용할 에러 속성들을 수동으로 설정
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_UNAUTHORIZED);
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Unauthorized");  // 발생한 예외 객체
            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new AccessDeniedException(exceptionMessage));  // 사용자에게 보여줄 메시지
            request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, request.getRequestURI());  // 원래 요청 URI
            // 3. Spring Boot의 기본 에러 핸들러(BasicErrorController)가 /error 요청을 처리하고 error.jsp를 렌더링
            request.getRequestDispatcher("/error").forward(request, response);
        }
    }
}

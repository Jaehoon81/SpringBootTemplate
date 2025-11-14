package kr.co.jaehoon.springboottemplate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.ApprovalRequestDTO;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import kr.co.jaehoon.springboottemplate.dto.network.BasicResponse;
import kr.co.jaehoon.springboottemplate.dto.network.ErrorResponse;
import kr.co.jaehoon.springboottemplate.dto.network.MobileAuthRequest;
import kr.co.jaehoon.springboottemplate.dto.network.MobileAuthResponse;
import kr.co.jaehoon.springboottemplate.dto.validation.FindAccountRequest;
import kr.co.jaehoon.springboottemplate.dto.validation.RegistrationRequest;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import kr.co.jaehoon.springboottemplate.security.JwtBlacklistService;
import kr.co.jaehoon.springboottemplate.service.AuthRestService;
import kr.co.jaehoon.springboottemplate.service.UserService;
import kr.co.jaehoon.springboottemplate.service.impl.UserDetailsServiceImpl;
import kr.co.jaehoon.springboottemplate.security.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Authentication API", description = "웹/모바일 인증(회원가입, 계정찾기, 로그인/로그아웃) 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;

//    private final UserDAO userDAO;
//    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthRestService authRestService;

    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 API (웹/모바일 공용)
     * ADMIN 또는 USER 권한의 경우에만 /api/auth/register 엔드포인트로 회원 가입이 가능
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegistrationRequest registrationRequest, BindingResult bindingResult
    ) throws Exception {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(
                    fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()
            ));
            log.warn("Auth-Register_BindingResult_InvalidException: {}", errors.toString());
            return ResponseEntity.badRequest().body(errors);
        }
        // 1. 아이디 중복 확인
        UserDTO user1 = userService.findByUsername(registrationRequest.getUsername());
        if (user1 != null) {
            log.warn("Auth-Register_UserDTO_Username: {}", user1.getUsername());
            return ResponseEntity.badRequest().body("이미 사용 중인 아이디입니다.");
        }
        // 2. 이름 중복 확인
        UserDTO user2 = userService.findByDisplayname(registrationRequest.getDisplayname());
        if (user2 != null) {
            log.warn("Auth-Register_UserDTO_Displayname: {}", user2.getDisplayname());
            return ResponseEntity.badRequest().body("이미 사용 중인 이름입니다.");
        }
        // 3-1. 권한에 대한 유효성 검사 (USER, ADMIN만 허용)
        if (!registrationRequest.getRole().equals("USER") && !registrationRequest.getRole().equals("ADMIN")
//                && !registrationRequest.getRole().equals("SYSTEM")
        ) {
            return ResponseEntity.badRequest().body("잘못된 권한이 지정되었습니다.");
        }
        // 3-2. 권한 유무 확인
        Long roleId = userService.findRoleIdByRolename(registrationRequest.getRole().toUpperCase());
        if (roleId == null) {
            log.warn("Auth-Register_Long_RoleID: {}", (Object) null);
            return ResponseEntity.badRequest().body("유효하지 않은 권한입니다.");
        } else {
            registrationRequest.setRoleId(roleId);
        }
        // 4. UserDTO 객체 생성 및 데이터 매핑, DB의 users 테이블에 저장
        UserDTO newUser = new UserDTO();
        newUser.setUsername(registrationRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setDisplayname((registrationRequest.getDisplayname() != null) ? registrationRequest.getDisplayname() : "");
        newUser.setEmail(registrationRequest.getEmail());
//        newUser.setRole(registrationRequest.getRole());
        newUser.setRoleId(registrationRequest.getRoleId());
        newUser.setActiveSessionJti(null);  // 초기에는 active_session_jti 값 없음
        userService.saveUser(newUser);

        // 5. ApprovalRequestDTO 객체 생성 및 데이터 매핑
        ApprovalRequestDTO approvalRequest = new ApprovalRequestDTO();
        approvalRequest.setUserId(newUser.getId());  // 새로 생성된 UserDTO의 id 사용
        approvalRequest.setReqMessage(registrationRequest.getReqMessage());
        approvalRequest.setApproved(false);  // 가입 시 기본 값은 미승인

        // 6. USER 권한일 경우 adminname 필수 및 유효성 검사
        if ("USER".equalsIgnoreCase(registrationRequest.getRole())) {
            // 이 유효성 검사는 RegistrationRequest DTO의 @Pattern으로 대체될 수도 있지만,
            // 여기서는 명시적인 비즈니스 로직으로 분리하여 관리자가 선택되었는지 확인
            if (registrationRequest.getAdminname() != null && !registrationRequest.getAdminname().trim().isEmpty()) {
                Long adminId = userService.findAdminIdByDisplayname(registrationRequest.getAdminname());
                if (adminId == null) {
                    log.warn("Auth-Register_Long_AdminID: {}", (Object) null);
                    if (userService.deleteUser(newUser.getId()) == 1) {
                        log.debug("The 'users' table has been rolled back(1).");
                    }
                    return ResponseEntity.badRequest().body("존재하지 않거나 승인되지 않은 관리자입니다.");
                } else {
                    approvalRequest.setAssignedAdminId(adminId);
                }
            } else {  // registrationRequest.getAdminname() == null || registrationRequest.getAdminname().trim().isEmpty()
                approvalRequest.setAssignedAdminId(null);  // USER 권한의 담당 관리자 미지정 시 null
                if (userService.deleteUser(newUser.getId()) == 1) {
                    log.debug("The 'users' table has been rolled back(2).");
                }
                return ResponseEntity.badRequest().body("일반 사용자 선택 시 담당 관리자를 선택해야 합니다.");
            }
            // 선택된 adminname이 실제 ADMIN 권한의 displayname으로 존재하는지 확인
            // (displayname은 중복될 수 있으므로 추후에는 ID 기반으로 선택하는 것을 고려)
            Integer adminCount = userService.countAdminByDisplayname(registrationRequest.getAdminname());
            if (adminCount == 0) {
                log.warn("Auth-Register_Integer_AdminCount: {}", adminCount.toString());
                if (userService.deleteUser(newUser.getId()) == 1) {
                    log.debug("The 'users' table has been rolled back(3).");
                }
                return ResponseEntity.badRequest().body("선택된 담당 관리자가 존재하지 않습니다.");
            }
        } else {  // ADMIN 권한일 경우
            registrationRequest.setAdminname(null);  // adminname은 null 또는 비어있는 상태로 저장
            approvalRequest.setAssignedAdminId(null);  // ADMIN 권한으로 요청 시 null
        }
        // 7. DB의 approval_requests 테이블에 저장
        userService.saveApprovalRequest(approvalRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("회원 가입이 성공적으로 완료되었습니다!");
    }

    /**
     * ADMIN 권한의 displayname 목록을 반환하는 API
     */
    @GetMapping("/admins")
    public ResponseEntity<List<String>> getAdminNames() throws Exception {
        List<String> displaynames = userService.findAdminNames();
        log.debug("Auth-Admins_DisplaynameList: {}", displaynames.toString());

        return ResponseEntity.ok(displaynames);
    }

    /**
     * 아이디/비밀번호 찾기(이메일 발송) API
     */
    @PostMapping("/find-account")
    public ResponseEntity<?> findAccount(
            @Valid @RequestBody FindAccountRequest request, BindingResult bindingResult
    ) throws Exception {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(
                    fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()
            ));
            log.warn("Auth-FindAccount_BindingResult_InvalidException: {}", errors.toString());
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            authRestService.findAccountAndResetPassword(request);
            return ResponseEntity.ok("아이디와 임시 비밀번호가 이메일로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("general", e.getMessage());  // general 오류 메시지로 프론트엔드에 전달

            log.warn("Auth-FindAccount_General_InvalidException(1): {}", errors.toString());
            return ResponseEntity.badRequest().body(errors);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("general", "이메일 전송 중 오류가 발생했습니다.\n유효한 이메일 주소가 아닐 수 있습니다.");

            log.error("Auth-FindAccount_General_InvalidException(2): {}", errors.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
        }
    }

    /**
     * 웹 로그인 API (쿠키 기반 인증)
     * 웹 브라우저는 로그인 성공 시 HttpOnly 쿠키를 전달받음
     */
    @PostMapping("/web-login")
    public ResponseEntity<?> webLogin(
            @RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response
    ) throws Exception {
        try {
            // 1. Spring Security의 AuthenticationManager를 통해 사용자가 유효한지 검증
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()
            ));
            // 인증 정보를 SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException e) {
            // 인증 실패 시 (아이디 or 비밀번호 불일치)
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        // 2. 인증 성공 후 UserDetails를 로드하여 사용자 정보 확인
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        // 3. 권한 확인: 웹 로그인은 SYSTEM or ADMIN 권한만 허용
        if (userDetails != null && userDetails instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            if ("USER".equalsIgnoreCase(customUserDetails.getUser().getRole())) {
                // USER 권한일 경우 403 Forbidden 응답 반환
                // (USER 권한의 계정이 웹 브라우저에서도 로그인이 가능하도록 주석처리)
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body("일반 사용자는 웹 브라우저로 로그인할 수 없습니다.\n모바일 앱을 이용해주세요.");
            }
        } else {
            // CustomUserDetails가 아닌 경우 500 Internal_Server_Error 응답 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 권한 정보를 확인할 수 없습니다.");
        }
        // 4. SYSTEM or ADMIN 권한 확인 완료 시 JWT 생성 및 반환
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), false);

        // 5. JWT를 HttpOnly 쿠키로 추가
        Cookie jwtCookie = new Cookie("jwtToken", jwt);  // 쿠키 이름은 'jwtToken'
        jwtCookie.setHttpOnly(true);  // JavaScript에서 접근 불가
        jwtCookie.setPath("/");  // 모든 경로에서 유효하도록 설정
        jwtCookie.setMaxAge((int) (jwtUtil.getWebExpirationTime() / 1000));  // JWT 만료 시간과 동일하게 설정 (초 단위)
        jwtCookie.setSecure(true);  // HTTPS 환경에서 필수
        response.addCookie(jwtCookie);

        // 프론트엔드에서 jwtToken을 body로 받을 필요 없으므로 빈 응답 반환 또는 성공 메시지만 반환
//        return ResponseEntity.ok(new AuthenticationResponse(
//                jwt, userDetails.getUsername(), "로그인(웹) 성공"
//        ));
        return ResponseEntity.ok().body("로그인(웹) 성공");
    }

    /**
     * 모바일 로그인 API (JWT 응답 본문 전달)
     * 모바일 앱은 로그인 성공 시 Access Token을 응답 본문에서 파싱하여 저장함
     * 사용자가 인증(아이디와 비밀번호 검증)에 성공하고, USER 권한을 가지고 있을 경우에만 실행
     * SYSTEM이나 ADMIN 권한의 사용자는 로그인 시도 시 403 Forbidden 응답을 받게 됨
     *
     * -> 사용하지 않음: @PreAuthorize 어노테이션은 이미 인증된 사용자가 특정 리소스에 접근할 때 권한이 충분한지 검사하는데 사용됨
     * 로그인 시도 자체는 아직 Authentication이 완전히 확립되지 않은 상태이기 때문에 사용자의 권한 정보를 불러올 수가 없어서
     * 로그인 시도에는 일반적으로 @PreAuthorize 어노테이션 방식을 사용하지 않음
     */
//    @PostMapping("/mobile-login")
//    @PreAuthorize("hasRole('USER')")  // USER 권한만 허용
//    public ResponseEntity<?> mobileLogin(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
//        UserDetails userDetails = null;
//        try {
//            // Spring Security의 AuthenticationManager를 통해 사용자 인증을 수행
//            // (인증 성공 시 Spring Security는 Authentication 객체를 생성하고,
//            // 내부적으로 SecurityContextHolder에 이를 저장하여 @PreAuthorize와 같은 AOP 기반 보안이 동작하도록 함)
//            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                    authenticationRequest.getUsername(), authenticationRequest.getPassword()
//            ));
////            SecurityContextHolder.getContext().setAuthentication(authentication);
//            // 인증된 Principal(UserDetails)을 가져옴
//            userDetails = (UserDetails) authentication.getPrincipal();
//        } catch (AuthenticationException e) {  // AuthenticationException을 Catch하여 BadCredentialsException 포함
//            // 인증 실패 시 (아이디 or 비밀번호 불일치)
//            String errorMessage = e.getMessage();
//            log.warn(errorMessage, e);
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
//        }
//        // @PreAuthorize가 이미 권한 검사를 수행했으므로 별도의 권한 확인을 위한 로직이 필요 없음
//        // JWT 생성 및 반환 (이미 USER 권한임을 보장)
//        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), true);
//        // 생성된 토큰에서 JTI를 추출하여 DB의 해당 계정에 업데이트
//        final String jti = jwtUtil.extractJti(jwt);
//        userService.updateActiveSessionJti(((CustomUserDetails) userDetails).getUser().getId(), jti);
//
//        // 모바일 클라이언트에게 JWT(Access Token)를 응답 본문에 포함하여 반환
//        return ResponseEntity.ok(new AuthenticationResponse(
//                jwt, userDetails.getUsername(), "로그인(모바일) 성공"
//        ));
//    }

    /**
     * 모바일 로그인 API (JWT 응답 본문 전달)
     * 모바일 앱은 로그인 성공 시 Access Token을 응답 본문에서 파싱하여 저장함
     */
    @Operation(summary = "모바일 로그인",
            description = "모바일 앱에서 사용자를 인증하고 JWT 토큰을 발급합니다.",
            parameters = {
                    @Parameter(name = "username", description = "사용자 아이디", required = false),
                    @Parameter(name = "password", description = "비밀번호", required = false) })
    @ApiResponse(responseCode = "200",
            description = "로그인(모바일) 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MobileAuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "접근 거부")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping("/mobile-login")
    public ResponseEntity<?> mobileLogin(@RequestBody MobileAuthRequest authenticationRequest) throws Exception {
        try {
            // 1. Spring Security의 AuthenticationManager를 통해 사용자가 유효한지 검증
            // (stateless이므로 SecurityContextHolder에 저장하지 않음)
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()
            ));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException e) {
            // 인증 실패 시 (아이디 or 비밀번호 불일치)
            log.warn(e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        // 2. 인증 성공 후 UserDetails를 로드하여 사용자 정보 확인
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        // 3. 권한 확인: 모바일 로그인은 USER 권한만 허용
        if (userDetails != null && userDetails instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            if (!"USER".equalsIgnoreCase(customUserDetails.getUser().getRole())) {
                // USER 권한이 아닐 경우 403 Forbidden 응답 반환
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("모바일 앱에서는 일반 사용자만 로그인할 수 있습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BasicResponse.failure(
                        ErrorResponse.from(403, "Forbidden",
                                "접근 권한이 없습니다.", "모바일 앱에서는 일반 사용자만 로그인할 수 있습니다.")
                ));
            }
        } else {
            // CustomUserDetails가 아닌 경우 500 Internal_Server_Error 응답 반환
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 권한 정보를 확인할 수 없습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.failure(
                    ErrorResponse.from(500, "Internal_Server_Error",
                            "서버 오류가 발생했습니다.", "사용자 권한 정보를 확인할 수 없습니다.")
            ));
        }
        // 4. USER 권한 확인 완료 시 JWT 생성 및 반환
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), true);
        // 5. 생성된 토큰에서 JTI를 추출하여 DB의 해당 계정에 업데이트
        final String jti = jwtUtil.extractJti(jwt);
        userService.updateActiveSessionJti(((CustomUserDetails) userDetails).getUser().getId(), jti);

        // 모바일 클라이언트에게 JWT(Access Token)를 응답 본문에 포함하여 반환
        return ResponseEntity.status(HttpStatus.OK).body(BasicResponse.success(
                MobileAuthResponse.from(jwt, (CustomUserDetails) userDetails, "로그인(모바일) 성공")
        ));
    }

    /**
     * 웹 로그아웃 API (쿠키 기반 요청 및 문자열 응답)
     * 웹에서 쿠키 기반의 요청에 대해 문자열 응답을 보내고, 쿠키를 삭제
     * 클라이언트는 요청 시 Access Token을 쿠키에 담아 전송
     */
    @PostMapping("/web-logout")
    public ResponseEntity<?> webLogout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String jwt = null;
        // 쿠키에서 JWT 추출 (웹 브라우저의 자동 쿠키 전송)
        if (jwt == null && request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        performLogout(jwt, response);  // 공통 로그아웃 로직 호출

        // Spring Security의 Context에서 현재 인증 정보를 클리어
        SecurityContextHolder.clearContext();
        // 웹 클라이언트를 위해 텍스트 응답
        return ResponseEntity.ok().body("로그아웃(웹) 성공");
    }

    /**
     * 모바일 로그아웃 API (Authorization 헤더 기반 요청 및 JSON 응답)
     * 모바일에서 Authorization 헤더 기반의 요청에 대해 JSON 응답을 보내고, Access Token을 블랙리스트 처리
     * 클라이언트는 요청 시 Access Token을 Authorization 헤더에 담아 전송
     */
    @Operation(summary = "모바일 로그아웃",
            description = "모바일 앱 사용자를 로그아웃하고 JWT 토큰을 무효화합니다.",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT 토큰 (Bearer)", required = true, example = "Bearer <token>") })
    @ApiResponse(responseCode = "200",
            description = "로그아웃(모바일) 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MobileAuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 누락 또는 유효하지 않음)")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping(value = "/mobile-logout", produces = MediaType.APPLICATION_JSON_VALUE)  // JSON 응답 강제
    public ResponseEntity<?> mobileLogout(
            HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal CustomUserDetails userDetails,
            // Swagger UI에서 아래 파라미터를 보고 Authorization 헤더 입력 필드를 생성
//            @Parameter(name = "Authorization", description = "JWT 토큰 (Bearer)", required = true, example = "Bearer <token>")
            @RequestHeader(name = "Authorization", required = false) String authHeader  // required = false: 헤더가 없어도 호출 가능하게 함
    ) throws Exception {
        String jwt = null;
        // Authorization 헤더에서 JWT 추출 (모바일에서 명시적으로 보낸 경우)
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 토큰이 누락되었거나 유효하지 않습니다.");
            throw new BadCredentialsException("인증 토큰이 누락되었거나 유효하지 않습니다.");
        }
        performLogout(jwt, response);  // 공통 로그아웃 로직 호출

        if (userDetails != null && userDetails.getUser().getId() != null) {
            // DB의 해당 계정에 active_session_jti 컬럼을 null로 업데이트하여 해당 토큰을 무효화
            userService.updateActiveSessionJti(userDetails.getUser().getId(), null);
        }
        if (userDetails != null) {
            // Spring Security의 Context에서 현재 인증 정보를 클리어
            SecurityContextHolder.clearContext();
            // 모바일 클라이언트에게 JWT(Access Token)를 응답 본문에 포함하지 않고 반환
            return ResponseEntity.status(HttpStatus.OK).body(BasicResponse.success(
                    MobileAuthResponse.from("", userDetails, "로그아웃(모바일) 성공")
            ));
        } else {  // userDetails == null
            // userDetails가 null이라면 토큰이 이미 만료됐을 가능성이 있으므로
            // 아래와 같이 500 에러 응답은 반환하지만 자동 로그아웃 됨
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 누락 또는 토큰이 만료되었을 수 있습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.failure(
                    ErrorResponse.from(500, "Internal_Server_Error",
                            "서버 오류가 발생했습니다.", "사용자 정보 누락 또는 토큰이 만료되었을 수 있습니다.")
            ));
        }
    }

    /**
     * 로그아웃 시 JWT 쿠키 삭제 및 블랙리스트 처리 공통 로직
     */
    private void performLogout(String jwt, HttpServletResponse response) throws Exception {
        // JWT가 존재하면 검증 시작
        if (jwt != null) {
            try {
                Date expiration = jwtUtil.extractExpiration(jwt);
                // JWT를 블랙리스트에 추가하여 즉시 무효화 (모바일/웹 토큰 모두 해당)
//                jwtBlacklistService.addTokenToBlacklist(jwt, expiration.getTime());  // 간단한 인메모리 캐시를 사용
                jwtBlacklistService.addTokenToBlacklist(jwt, expiration);  // MySQL DB 기반 영속적인 방식
            } catch (Exception e) {
                // 토큰 추출 실패 또는 이미 만료된 토큰인 경우에도 로그아웃 처리 진행 (블랙리스트에 추가는 불필요)
//                System.out.println("로그아웃 처리 중 토큰 오류: " + e.getMessage());
                log.warn("JWT token error during logout processing: {}", e.getMessage());
            }
        }
        // 웹 클라이언트를 위해 JWT 쿠키 삭제 (브라우저에서 해당 쿠키를 더이상 전송하지 않음)
        Cookie jwtCookie = new Cookie("jwtToken", "");  // 빈 값으로 설정
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);  // 유효기간을 0으로 설정하여 즉시 만료
        jwtCookie.setSecure(true);
        response.addCookie(jwtCookie);
    }

    /**
     * JWT Access Token 유효성 검사 (모바일 앱에서 세션만료 여부 확인 용도)
     * /api/auth/check-token 엔드포인트에 성공적으로 접근 -> Access Token이 유효함
     */
    @GetMapping("/check-token")
    public ResponseEntity<Map<String, String>> checkTokenValidity() {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("username", SecurityContextHolder.getContext().getAuthentication().getName());
        responseMap.put("resMessage", "Access Token이 유효합니다.");

//        return ResponseEntity.ok(responseMap);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseMap);
    }

    @Data
    static class AuthenticationRequest {  // 로그인 요청 데이터 (웹/모바일 공용)
        private String username;
        private String password;
    }

    // AuthenticationResponse 클래스 필요 없음 (JWT를 body로 보내지 않으므로)
    @Data
    static class AuthenticationResponse {  // 로그인 응답 데이터 (웹/모바일 공용)
        private final String accessToken;
        private final String username;
        private final String resMessage;
    }
}

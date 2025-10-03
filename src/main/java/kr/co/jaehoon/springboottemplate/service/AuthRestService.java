package kr.co.jaehoon.springboottemplate.service;

import jakarta.mail.MessagingException;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.dto.validation.FindAccountRequest;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//@Slf4j
public class AuthRestService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    private final UserDAO userDAO;
//    private final UserRepository userRepository;
    private final UserService userService;

    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void findAccountAndResetPassword(
            FindAccountRequest request
    ) throws IllegalArgumentException, MessagingException, Exception {
        UserDTO user = userService.findUserByDisplaynameAndEmail(request.getDisplayname(), request.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("이름 또는 이메일과 일치하는 계정을 찾을 수 없습니다.");
        } else {
            log.debug("Find-Account_UserDTO_Username: {}", user.getUsername());
        }
        // 1. 새 임시 비밀번호 생성
        String newRandomPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(newRandomPassword);
        // 2. DB users 테이블의 password 컬럼에 임시 비밀번호를 업데이트
        userService.updatePassword(user.getId(), encodedPassword);
        // 3. 아이디 마스킹
        String maskedUsername = maskUsername(user.getUsername());

        // 4. 마스킹된 아이디와 임시 비밀번호를 해당 이메일 주소로 발송
        String subject = "[Spring-Boot Template 서비스] 아이디/임시 비밀번호 안내";
//        String text = "안녕하세요, <b>" + user.getDisplayname() + "</b>님.<br/>"
//                + "요청하신 계정 정보입니다.<br/><br/>"
//                + "아이디: <b>" + maskedUsername + "</b><br/>"
//                + "임시 비밀번호: <b>" + newRandomPassword + "</b><br/><br/>"
//                + "로그인 후 반드시 비밀번호를 변경해주세요!<br/>"
//                + "감사합니다.";
//        emailService.sendTextEmail(user.getEmail(), subject, text);
        String htmlContent = "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; color: #333;'>"
                + "<h3 style='font-size: 1.05em;'>안녕하세요, <strong style='color: #2E86C1;'>" + user.getDisplayname() + "</strong>님.</h3>"
                + "<p style='margin-bottom: 20px;'>요청하신 계정 정보입니다.</p>"
                + "<table style='border-collapse: collapse; width: 300px;'>"
                + "  <tr>"
                + "    <td style='width: 40%; padding: 8px; font-weight: bold; border: 1px solid #ccc; background-color: #EAEAEA;'>아이디:</td>"
                + "    <td style='width: 60%; padding: 8px; border: 1px solid #ccc; font-size: 1.05em; font-weight: bold; color: #2E86C1;'>" + maskedUsername + "</td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td style='width: 40%; padding: 8px; font-weight: bold; border: 1px solid #ccc; background-color: #EAEAEA;'>임시 비밀번호:</td>"
                + "    <td style='width: 60%; padding: 8px; border: 1px solid #ccc; font-size: 1.05em; font-weight: bold; color: #D64541;'>" + newRandomPassword + "</td>"
                + "  </tr>"
                + "</table>"
                + "<p style='margin-top: 20px; font-weight: bold; color: #D64541;'>로그인 후 반드시 비밀번호를 변경해주세요!</p>"
                + "<p>감사합니다.<br/>[Spring-Boot Template 서비스] 드림</p>"
                + "</body></html>";
        emailService.sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    private String generateRandomPassword() {
        // 10자 이상의 영문, 숫자, 특수문자를 모두 포함
        final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        final String NUMBER = "0123456789";
//        final String SPECIAL = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        final String SPECIAL = "!@#$%^&*_+-=|;':\",./<>?";

        final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);  // 최소 10자, 여기서는 12자로 시작

        // 각 유형별 최소 1자가 포함됨을 보장
        sb.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        sb.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        sb.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        sb.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        // 나머지 길이 채우기
        for (int i = 0; i < 8; i++) {  // 총 12자 (4 + 8)
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        // 무작위로 섞기
        List<Character> chars = sb.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        Collections.shuffle(chars, random);
        return chars.stream().map(String::valueOf).collect(Collectors.joining());
    }

    private String maskUsername(String username) {
        if (username == null || username.length() < 2) {
            return username;  // 길이가 너무 짧으면 마스킹 불가능
        }
        int maskLength = username.length() / 2;  // 앞자리 절반만 마스킹
        return username.substring(0, maskLength) + "*".repeat(username.length() - maskLength);
    }
}

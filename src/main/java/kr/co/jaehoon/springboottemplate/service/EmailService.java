package kr.co.jaehoon.springboottemplate.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
//@Slf4j
public class EmailService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final JavaMailSender mailSender;

    // TEXT 형식으로 이메일을 보낼 때 사용하는 메서드
    public void sendTextEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    // HTML 형식으로 이메일을 보낼 때 사용하는 메서드
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        // true: multipart 메시지 허용, UTF-8 인코딩 사용을 위해 설정
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);  // true: HTML 형식임을 지정
        mailSender.send(message);
    }
}

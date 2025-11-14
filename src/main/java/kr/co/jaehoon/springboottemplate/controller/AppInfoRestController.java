package kr.co.jaehoon.springboottemplate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app-info")
@RequiredArgsConstructor
@Slf4j
public class AppInfoRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.version}")
    private String appVersion;

    /**
     * 애플리케이션 버전 정보 (모바일 앱 요청 테스트용)
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getAppVersion() {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("version", appVersion);
        responseMap.put("resMessage", "애플리케이션 버전 정보입니다.");

//        return ResponseEntity.ok(responseMap);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseMap);
    }
}

package kr.co.jaehoon.springboottemplate.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {  // UserRestController에서 직접 파일을 서빙하는 방식을 유지 (사용하지 않음)

    @Value("${file.upload-dir}")  // 파일 업로드 디렉토리
    private String uploadDir;

    // Spring Boot와 Tomcat이 파일 시스템의 특정 경로에 있는 파일을 웹으로 서빙하려면 그 경로를 웹 애플리케이션의 정적 리소스 경로로 매핑해 주어야 한다.
    // /profiles/ 경로로 시작하는 URL을 컨트롤러가 아닌 정적 리소스로 직접 접근 가능하도록 설정할 수도 있다.
    // (WebConfig.java(Configuration 클래스)를 만들어 /uploads/** 경로를 외부 디렉토리로 매핑하는 것이 더 일반적인 방법임)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 경로로 요청이 들어오면 file.upload-dir 디렉토리에서 파일을 찾을 수 있게 매핑
        // (예: file.upload-dir=D:/Web/Spring/Workspace/SpringBootTemplate/uploads/)
        registry.addResourceHandler("/profiles/**")  // 웹에서 /profiles/ 경로로 요청이 들어오면
                .addResourceLocations("file:" + uploadDir + "/profiles/");  // 실제 파일 시스템이 해당 경로를 찾을 수 있게 함
    }

    // 동일한 도메인(www.jaehoon.link)이라 할지라도 https와 http는 다른 Origin으로 간주되므로
    // 웹 브라우저는 보안상의 이유로 https 페이지에서 http 리소스에 접근하는 것을 기본적으로 차단한다.(Mixed Content Blocked)
    // https 페이지에서 http로 시작되는 잘못된 스킴으로 API를 호출하려고 시도하는 경우
    // 스킴이 다르기 때문에 웹 브라우저는 이 요청을 Cross-Origin 요청으로 분류하고 CORS 정책을 적용한다.

    // CORS 설정: 웹 브라우저가 API를 호출할 때, Cross-Origin 요청으로 분류하고 CORS 정책을 적용하는 것을 방지하기 위해
    // 서버(Spring Boot)가 특정 Origin을 설정하여 Cross-Origin 요청을 명시적으로 허용하도록 한다.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 API 경로를 대상으로
                .allowedOrigins("https://www.jaehoon.link", "http://localhost:8181")  // 실제 배포되는 도메인 또는 개발 서버 도메인
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 허용할 HTTP 메서드
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true)  // 쿠키와 같은 자격증명 허용 (필요한 경우)
                .maxAge(3600);  // Pre-flight 요청 캐싱 시간
    }
}

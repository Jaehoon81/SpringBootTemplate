package kr.co.jaehoon.springboottemplate.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {  // UserRestController에서 직접 파일을 서빙하는 방식을 유지 (사용하지 않음)

    @Value("${file.upload-dir}")  // 파일 업로드 디렉토리
    private String uploadDir;

    // Spring Boot와 Tomcat이 파일 시스템의 특정 경로에 있는 파일을 웹으로 서빙하려면 그 경로를 웹 애플리케이션의 정적 리소스 경로로 매핑해 주어야 한다.
    // /profiles/로 시작하는 URL을 컨트롤러가 아닌 정적 리소스로 직접 접근 가능하도록 설정할 수도 있다.
    // (WebConfig.java(Configuration 클래스)를 만들어 /uploads/** 경로를 외부 디렉토리로 매핑하는 것이 더 일반적인 방법임)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 경로로 요청이 들어오면 file.upload-dir 디렉토리에서 파일을 찾을 수 있게 매핑
        // (예: file.upload-dir=D:/Web/Spring/Workspace/SpringBootTemplate/uploads/)
        registry.addResourceHandler("/profiles/**")  // 웹에서 /profiles/ 경로로 요청이 들어오면
                .addResourceLocations("file:" + uploadDir + "/profiles/");  // 실제 파일 시스템이 해당 경로를 찾을 수 있게 함
    }
}

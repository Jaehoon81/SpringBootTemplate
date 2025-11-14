package kr.co.jaehoon.springboottemplate;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

// SpringBootServletInitializer를 상속받아 외부 Tomcat/Servlet 컨테이너에서 WAR 파일을 실행할 수 있도록 합니다.
// 하지만 Spring Boot의 executable WAR는 java -jar 명령어로도 실행 가능합니다.
public class ServletInitializer extends SpringBootServletInitializer {

    // 이 메서드는 외부 Servlet 컨테이너에 의해 애플리케이션이 배포될 때 호출됩니다.
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SpringBootTemplateApplication.class);
    }

}

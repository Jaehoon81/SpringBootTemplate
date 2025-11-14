# 1. 빌드용 이미지 (Builder Stage)
FROM amazoncorretto:21 AS build

WORKDIR /app

# 빌드 시 Gradle 캐시용 볼륨 선언 (필요에 따라)
#VOLUME /root/.gradle

# Gradle Wrapper 및 설정 파일 복사 (캐시 레이어 최적화)
# 이 파일들이 변경되지 않는 한 다음 레이어들은 캐시 재활용
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle Wrapper 실행 권한 추가
RUN chmod +x ./gradlew

# Gradle 의존성 캐싱 (초기 빌드 시간을 단축)
# --no-daemon: 컨테이너 내부에서 Gradle 데몬을 실행하지 않음 (권장)
# || true: 의존성 다운로드에 실패하더라도 빌드 프로세스를 중단하지 않음 (배포 환경에서는 주의 필요)
RUN ./gradlew dependencies --no-daemon || true

# 모든 소스코드 복사 및 애플리케이션 빌드 (WAR 파일 생성)
COPY src ./src
# WAR 빌드 시 src/main/webapp에 있는 파일들은 WAR 파일 안에 자동으로 패키징됨 (build.gradle에 bootWar 태스크가 있어야 함)
# -x test: 테스트를 실행하지 않음 (CI/CD 파이프라인에서 별도로 테스트를 수행할 경우 유용)
#RUN ./gradlew build -x test --no-daemon
RUN ./gradlew bootWar -x test --no-daemon


# 2. 실행용 이미지 (Runtime Stage) - 불필요한 빌드 환경 제거
# Spring Boot의 executable WAR는 자체적으로 내장 Tomcat을 포함하므로 별도의 Tomcat 이미지 없이 JDK 이미지만으로도 실행 가능
# 더 가벼운 JRE-only 이미지 사용을 고려할 수도 있음 (amazoncorretto:21-alpine)
FROM amazoncorretto:21

WORKDIR /app

# 빌드 단계에서 생성된 실행 가능한 JAR 또는 WAR 파일만 복사 (경량화)
# 프로젝트 빌드 시 생성되는 실제 파일명에 맞춰서 수정
# (예: bootWar 블록에서 archiveFileName을 springboot-template.war로 설정 시, springboot-template.war -> app.war)
#COPY --from=build /app/build/libs/SpringBootTemplate-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/build/libs/springboot-template.war app.war

# 애플리케이션 포트 노출 (선택 사항이지만 Dockerfile의 명확성을 위해 포함)
# Spring Boot의 기본 포트를 8080으로 가정
EXPOSE 8080

# 컨테이너 시작 시 실행될 명령어 (Spring Boot의 executable WAR 파일을 실행)
# -Dspring.profiles.active=prod: Spring 프로파일을 prod로 설정
# -Duser.timezone=Asia/Seoul: 타임존을 한국 시간으로 설정
#CMD ["java", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
CMD ["java", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "-jar", "app.war"]

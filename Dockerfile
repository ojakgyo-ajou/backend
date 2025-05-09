FROM gradle:jdk21 AS build

# 작업 디렉토리 설정
WORKDIR /home/gradle/project

# Gradle 의존성 캐시를 위한 레이어 분리
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradlew ./
RUN gradle dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY --chown=gradle:gradle src ./src
RUN gradle build --no-daemon

# 실행 단계
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

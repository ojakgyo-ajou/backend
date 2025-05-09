FROM gradle:jdk21 AS build

WORKDIR /home/gradle/project

# Gradle 의존성 캐시를 위한 레이어 분리
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle gradlew ./
RUN gradle dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY --chown=gradle:gradle src ./src
RUN gradle clean bootJar --no-daemon --parallel

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

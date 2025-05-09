FROM gradle:jdk21 AS build

WORKDIR /tmp

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle build.gradle settings.gradle .
RUN gradle dependencies --no-daemon

COPY --chown=gradle:gradle src ./src
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
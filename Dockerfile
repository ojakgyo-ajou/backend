FROM openjdk:21
WORKDIR /app
COPY build/libs/Ojakgyo-0.0.1-SNAPSHOT.jar backend.jar
EXPOSE 8080
CMD ["java", "-jar", "backend.jar"]
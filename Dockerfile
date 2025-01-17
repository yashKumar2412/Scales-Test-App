FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/test-0.0.1-SNAPSHOT.jar scales-test-app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "scales-test-app.jar"]
FROM openjdk:21-slim
MAINTAINER inaumov
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
FROM openjdk:8-jdk-alpine
ARG JAR=target/*.jar
COPY ${JAR} timetable-app.jar
ENTRYPOINT ["java", "-jar", "/timetable-app.jar"]
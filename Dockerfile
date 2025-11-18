# Этап 1: сборка war
#FROM maven:3.9-eclipse-temurin-17 AS builder
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#RUN mvn clean package -DskipTests

# Этап 2: запуск в Tomcat
FROM tomcat:10.1-jdk17-temurin
# удаляем стандартные приложения Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# копия war
COPY target/api.war /usr/local/tomcat/webapps/ROOT.war

# копия logback.xml
COPY src/main/resources/scripts/logback.xml /usr/local/tomcat/lib/logback.xml

CMD ["catalina.sh", "run"]
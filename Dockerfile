# FROM tomcat:10.1-jdk17
#
# RUN rm -rf /usr/local/tomcat/webapps/*
#
# COPY target/api.war /usr/local/tomcat/webapps/api.war
#
# COPY tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
#
# COPY context.xml /usr/local/tomcat/conf/context.xml
#
# EXPOSE 8080
#
# CMD ["catalina.sh", "run"]
# Этап 1: сборка
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src/ src/
RUN mvn clean package -DskipTests

# Этап 2: запуск
FROM tomcat:10.1-jdk17
# Удаляем стандартные приложения
RUN rm -rf /usr/local/tomcat/webapps/*

# Копируем WAR
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/api.war

# Настройки безопасности и БД
COPY tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
COPY context.xml /usr/local/tomcat/conf/context.xml

EXPOSE 8080
CMD ["catalina.sh", "run"]
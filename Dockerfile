# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM tomcat:10.1-jre17
# Удаляем дефолтный ROOT.war
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Копируем ваш WAR как ROOT
COPY --from=builder /app/target/secure-api.war /usr/local/tomcat/webapps/ROOT.war

# (Опционально) включаем логи в stdout
RUN sed -i 's/<Context>/<Context reloadable="true">/' /usr/local/tomcat/conf/context.xml

EXPOSE 8080
CMD ["catalina.sh", "run"]
FROM tomcat:10.1-jdk17

RUN rm -rf C:/Users/Alina/Downloads/apache-tomcat-11.0.14-windows-x64/apache-tomcat-11.0.14/webapps/*

COPY target/api.war C:/Users/Alina/Downloads/apache-tomcat-11.0.14-windows-x64/apache-tomcat-11.0.14/webapps/api.war

EXPOSE 8080

COPY tomcat-users.xml C:/Users/Alina/Downloads/apache-tomcat-11.0.14-windows-x64/apache-tomcat-11.0.14/conf/tomcat-users.xml

CMD ["catalina.sh", "run"]
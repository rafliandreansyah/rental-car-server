FROM eclipse-temurin:17-jdk-alpine

EXPOSE 8080

ARG JAR_FILE=target/rental-car-server-0.0.1-SNAPSHOT.jar

ARG DATABASE_URL
ARG DATABASE_USERNAME
ARG DATABASE_PASSWORD

ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
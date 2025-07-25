FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /build/target/banda-1.0-SNAPSHOT.jar banda.jar
EXPOSE 8080
CMD ["java", "-jar", "banda.jar"]

FROM maven:3.9.9-eclipse-temurin-24 AS build
WORKDIR /app

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY gateway/pom.xml gateway/
COPY identity-service/pom.xml identity-service/
COPY inventory-service/pom.xml inventory-service/
COPY booking-service/pom.xml booking-service/
COPY review-service/pom.xml review-service/
COPY media-service/pom.xml media-service/

RUN mvn -pl media-service dependency:go-offline -B

COPY media-service/src media-service/src

RUN mvn -pl media-service package -DskipTests

FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/media-service/target/media-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

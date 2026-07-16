FROM maven:3.9.9-eclipse-temurin-24-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY gateway/pom.xml gateway/
COPY identity-service/pom.xml identity-service/
COPY inventory-service/pom.xml inventory-service/
COPY booking-service/pom.xml booking-service/
COPY review-service/pom.xml review-service/
COPY media-service/pom.xml media-service/

RUN mvn -pl identity-service dependency:go-offline -B

COPY identity-service/src identity-service/src

RUN mvn -pl identity-service package -DskipTests

FROM eclipse-temurin:24-jre-alpine
RUN apk add --no-cache bash
WORKDIR /app
COPY --from=build /app/identity-service/target/identity-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

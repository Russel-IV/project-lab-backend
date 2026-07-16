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

RUN mvn -pl booking-service dependency:go-offline -B

COPY booking-service/src booking-service/src

RUN mvn -pl booking-service package -DskipTests

FROM eclipse-temurin:24-jre-alpine
RUN apk add --no-cache bash
WORKDIR /app
COPY --from=build /app/booking-service/target/booking-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

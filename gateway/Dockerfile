# ==========================================
# STAGE 1: Build the Spring Boot Application
# ==========================================
# Multi-module Maven reactor build (docs/adr/0001 and on) — the build context is the
# repo root, not this directory, so every sibling module's pom.xml must be present for
# the reactor to resolve, even though only `gateway` gets compiled and packaged here.
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

# Pre-download dependencies for this module only; cached until gateway/pom.xml changes.
RUN mvn -pl gateway dependency:go-offline -B

COPY gateway/src gateway/src

# Compile and package just the gateway module.
RUN mvn -pl gateway package -DskipTests

# ==========================================
# STAGE 2: Lightweight Runtime Environment
# ==========================================
FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/gateway/target/gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

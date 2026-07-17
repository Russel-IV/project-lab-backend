# syntax=docker/dockerfile:1
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
COPY chatbot-service/pom.xml chatbot-service/

RUN --mount=type=cache,target=/root/.m2,id=m2-cache mvn -pl chatbot-service dependency:go-offline -B

COPY chatbot-service/src chatbot-service/src
COPY chatbot-service/FRUI-CONTEXT.md chatbot-service/FRUI-CONTEXT.md

RUN --mount=type=cache,target=/root/.m2,id=m2-cache mvn -pl chatbot-service package -DskipTests

FROM eclipse-temurin:24-jre-alpine
RUN apk add --no-cache bash
WORKDIR /app
COPY --from=build /app/chatbot-service/target/chatbot-service-*.jar app.jar
COPY --from=build /app/chatbot-service/FRUI-CONTEXT.md FRUI-CONTEXT.md
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]

# ==========================================
# STAGE 1: Build the Spring Boot Application
# ==========================================
# We use a Maven image packaged with Eclipse Temurin JDK 24 to build the project.
# This ensures that no matter what JDK version your teammates have on their machine,
# the code will always compile under Java 24 inside the container.
FROM maven:3.9.9-eclipse-temurin-24 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven description file (pom.xml) first.
# By copying pom.xml before our source code, Docker caches this step.
# It will skip downloading dependencies on subsequent builds unless pom.xml actually changes.
COPY pom.xml .

# Pre-download dependencies to speed up subsequent builds.
# Since this runs on the cached pom.xml layer, dependencies are stored in the Docker builder cache
# and do not need to be re-downloaded when your Kotlin source code changes.
RUN mvn dependency:go-offline -B

# Copy the actual source code of the backend
COPY src ./src

# Compile the code and package it into a runnable JAR file.
# We skip tests here to expedite the build, but they can be enabled if preferred.
RUN mvn clean package

# ==========================================
# STAGE 2: Lightweight Runtime Environment
# ==========================================
# We use a slim Eclipse Temurin JRE 24 image to run the JAR.
# This keeps the final production container extremely lightweight and secure.
FROM eclipse-temurin:24-jre

# Set the working directory for the running application
WORKDIR /app

# Copy only the compiled JAR file from the builder stage
COPY --from=build /app/target/*.jar app.jar

# Expose the standard port where Spring Boot listens (8080)
EXPOSE 8080

# Run the Spring Boot application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]

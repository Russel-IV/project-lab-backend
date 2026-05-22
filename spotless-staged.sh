#!/bin/bash
# =========================================================================
# Spotless Staged Files Formatter
# =========================================================================
# Converts space-separated staged files from lint-staged to comma-separated
# paths, and runs the Spotless Maven Plugin on them.
# =========================================================================

if [ $# -eq 0 ]; then
  exit 0
fi

# Join all arguments with a comma
FILES_COMMA=$(IFS=,; echo "$*")

# Run Spotless in a standalone Maven build container to avoid JRE runtime conflicts,
# mounting the project folder and caching Maven packages inside $HOME/.m2.
docker run --rm \
  -v "$(pwd):/app" \
  -v "$HOME/.m2:/root/.m2" \
  -w /app \
  maven:3.9.9-eclipse-temurin-24 \
  ./mvnw spotless:apply -DspotlessFiles="$FILES_COMMA"

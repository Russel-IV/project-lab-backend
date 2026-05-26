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

# Prefix each argument with "/app/" (the container's workspace root) and join with a comma.
# This ensures that Spotless inside the container matches absolute file paths correctly.
FILES_COMMA=""
for file in "$@"; do
  FILES_COMMA="${FILES_COMMA:+$FILES_COMMA,}/app/$file"
done

# Run Spotless in a standalone Maven build container to avoid JRE runtime conflicts,
# mounting the project folder and caching Maven packages inside $HOME/.m2.
docker run --rm \
  -v "$(pwd):/app" \
  -v "$HOME/.m2:/root/.m2" \
  -w /app \
  maven:3.9.9-eclipse-temurin-24 \
  ./mvnw spotless:apply -DspotlessFiles="$FILES_COMMA"

#!/bin/bash
# Bash script to download Gradle wrapper jar
# This fixes the missing gradle-wrapper.jar issue

GRADLE_VERSION="8.5"
WRAPPER_DIR="gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"
DOWNLOAD_URL="https://raw.githubusercontent.com/gradle/gradle/v$GRADLE_VERSION/gradle/wrapper/gradle-wrapper.jar"

echo "Downloading Gradle Wrapper $GRADLE_VERSION..."

# Create wrapper directory if it doesn't exist
mkdir -p "$WRAPPER_DIR"

# Download the wrapper jar
if curl -L -o "$WRAPPER_JAR" "$DOWNLOAD_URL"; then
    echo "✓ Successfully downloaded gradle-wrapper.jar"
    
    # Verify the file exists and has content
    FILE_SIZE=$(stat -c%s "$WRAPPER_JAR" 2>/dev/null || stat -f%z "$WRAPPER_JAR" 2>/dev/null)
    echo "✓ File size: $FILE_SIZE bytes"
    
    echo ""
    echo "Now you can rebuild the Docker images:"
    echo "  docker-compose -f docker-compose.dev.yml build --no-cache"
else
    echo "✗ Failed to download gradle-wrapper.jar"
    echo ""
    echo "Alternative: Download manually from:"
    echo "  https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    echo "  Extract and copy gradle/wrapper/gradle-wrapper.jar to $WRAPPER_DIR/"
    exit 1
fi

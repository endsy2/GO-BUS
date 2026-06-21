#!/bin/bash
# Fix missing gradle-wrapper.jar by downloading Gradle distribution

GRADLE_VERSION="8.5"
GRADLE_ZIP="gradle-$GRADLE_VERSION-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/$GRADLE_ZIP"
WRAPPER_DIR="gradle/wrapper"

echo "Fixing Gradle wrapper..."
echo "This will download Gradle $GRADLE_VERSION distribution (~100MB)"
echo ""

# Create wrapper directory
mkdir -p "$WRAPPER_DIR"

# Download Gradle distribution
echo "Downloading Gradle $GRADLE_VERSION..."
if curl -L -o "/tmp/$GRADLE_ZIP" "$GRADLE_URL"; then
    echo "✓ Downloaded Gradle distribution"
    
    # Extract only the wrapper jar
    echo "Extracting gradle-wrapper.jar..."
    unzip -j "/tmp/$GRADLE_ZIP" "gradle-$GRADLE_VERSION/lib/plugins/gradle-wrapper-$GRADLE_VERSION.jar" -d "/tmp/" 2>/dev/null
    
    # Try alternative path
    if [ ! -f "/tmp/gradle-wrapper-$GRADLE_VERSION.jar" ]; then
        unzip -j "/tmp/$GRADLE_ZIP" "gradle-$GRADLE_VERSION/gradle/wrapper/gradle-wrapper.jar" -d "/tmp/" 2>/dev/null
    fi
    
    # Copy to correct location
    if [ -f "/tmp/gradle-wrapper-$GRADLE_VERSION.jar" ]; then
        cp "/tmp/gradle-wrapper-$GRADLE_VERSION.jar" "$WRAPPER_DIR/gradle-wrapper.jar"
        echo "✓ Installed gradle-wrapper.jar"
    elif [ -f "/tmp/gradle-wrapper.jar" ]; then
        cp "/tmp/gradle-wrapper.jar" "$WRAPPER_DIR/gradle-wrapper.jar"
        echo "✓ Installed gradle-wrapper.jar"
    else
        echo "✗ Could not find gradle-wrapper.jar in distribution"
        echo "Extracting full distribution to find the jar..."
        unzip "/tmp/$GRADLE_ZIP" -d "/tmp/" 2>/dev/null
        find "/tmp/gradle-$GRADLE_VERSION" -name "gradle-wrapper*.jar" -exec cp {} "$WRAPPER_DIR/gradle-wrapper.jar" \;
    fi
    
    # Cleanup
    rm -f "/tmp/$GRADLE_ZIP"
    rm -rf "/tmp/gradle-$GRADLE_VERSION"
    
    # Verify
    if [ -f "$WRAPPER_DIR/gradle-wrapper.jar" ]; then
        FILE_SIZE=$(stat -c%s "$WRAPPER_DIR/gradle-wrapper.jar" 2>/dev/null || stat -f%z "$WRAPPER_DIR/gradle-wrapper.jar" 2>/dev/null)
        echo "✓ File size: $FILE_SIZE bytes"
        
        if [ "$FILE_SIZE" -gt "1000" ]; then
            echo ""
            echo "✓ Gradle wrapper fixed successfully!"
            echo ""
            echo "Now rebuild the Docker images:"
            echo "  docker-compose -f docker-compose.dev.yml build --no-cache"
        else
            echo "✗ File seems too small, something went wrong"
            exit 1
        fi
    else
        echo "✗ Failed to install gradle-wrapper.jar"
        exit 1
    fi
else
    echo "✗ Failed to download Gradle distribution"
    exit 1
fi

#!/bin/bash
# Fix line endings for gradlew files
# This script converts Windows line endings (CRLF) to Unix line endings (LF)

echo "Converting gradlew files to Unix line endings..."

# Check if dos2unix is available
if command -v dos2unix &> /dev/null; then
    echo "Using dos2unix..."
    dos2unix gradlew 2>/dev/null || echo "gradlew already has Unix line endings"
else
    echo "dos2unix not found, using sed..."
    # Use sed to remove carriage returns
    sed -i 's/\r$//' gradlew 2>/dev/null || sed -i '' 's/\r$//' gradlew 2>/dev/null
fi

# Make gradlew executable
chmod +x gradlew

echo "✓ Line endings fixed for gradlew"
echo "✓ gradlew is now executable"

#!/bin/bash
# Script to fix Gradle wrapper validation issues

# Function to retry a command
retry_command() {
    local cmd="$1"
    local max_retries=${2:-3}
    local retry=0
    
    until [ $retry -ge $max_retries ]
    do
        echo "Attempt $((retry+1)) to execute: $cmd"
        if eval "$cmd"; then
            echo "Command executed successfully"
            return 0
        else
            echo "Command failed, retrying in 5 seconds..."
            retry=$((retry+1))
            sleep 5
        fi
    done
    
    echo "Failed to execute command after $max_retries attempts"
    return 1
}

echo "Fixing Gradle wrapper validation issues..."

# Update Gradle wrapper with retry
if ! retry_command "./gradlew wrapper --gradle-version 8.11.1" 3; then
    echo "ERROR: Failed to update Gradle wrapper after multiple attempts"
    exit 1
fi

# Generate new checksums
cd gradle/wrapper

# Remove old checksum file
rm -f gradle-wrapper.jar.sha256

# Generate new checksum
sha256sum gradle-wrapper.jar > gradle-wrapper.jar.sha256

echo "Gradle wrapper fixed successfully!"
echo "New checksum: $(cat gradle-wrapper.jar.sha256)"
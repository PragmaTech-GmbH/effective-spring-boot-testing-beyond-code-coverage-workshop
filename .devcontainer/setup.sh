#!/bin/bash
set -e

echo "Setting up the workshop environment..."

# Configure Git
git config --global pull.rebase true
git config --global core.autocrlf input

# Install additional dependencies
echo "Installing additional dependencies..."
sudo apt-get update

echo "Pulling Docker images for testing..."
docker pull postgres:16-alpine
docker pull testcontainers/ryuk:0.11.0

# Add execution permission to Maven wrapper
echo "Making Maven wrapper executable..."
chmod +x ./mvnw

# Build the projects to download dependencies
echo "Building projects to download dependencies..."
for lab in labs/lab-1 labs/lab-2 labs/lab-3 labs/lab-4 labs/lab-5 labs/lab-6 labs/lab-7 labs/lab-8; do
    echo "Building $lab..."
    cd "$lab" || exit
    ../../mvnw dependency:resolve -DskipTests
    cd - || exit
done

echo "Setup complete! You're ready to start the workshop."

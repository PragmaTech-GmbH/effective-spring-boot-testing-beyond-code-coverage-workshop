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
docker pull axllent/mailpit:v1.20
docker pull quay.io/keycloak/keycloak:26.3
docker pull testcontainers/ryuk:0.13.0

# Add execution permission to Maven wrapper
echo "Making Maven wrapper executable..."
chmod +x ./mvnw

# Build the projects to download dependencies
echo "Building projects to download dependencies..."

./mvnw verify

echo "Setup complete! You're ready to start the workshop."

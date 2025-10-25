#!/bin/bash

# Smart Task Management API Generator Script
# This script generates OpenAPI documentation and client SDKs

set -e

echo "🚀 Starting API generation process..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    exit 1
fi

# Clean previous builds
print_status "Cleaning previous builds..."
mvn clean

# Compile the application
print_status "Compiling application..."
mvn compile

# Generate OpenAPI specification
print_status "Generating OpenAPI specification..."
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=openapi-gen" &
APP_PID=$!

# Wait for application to start
sleep 10

# Download OpenAPI spec
print_status "Downloading OpenAPI specification..."
curl -s http://localhost:8080/v3/api-docs > target/openapi.json

# Stop the application
kill $APP_PID 2>/dev/null || true
wait $APP_PID 2>/dev/null || true

if [ ! -f "target/openapi.json" ]; then
    print_error "Failed to generate OpenAPI specification"
    exit 1
fi

print_success "OpenAPI specification generated successfully"

# Generate client SDKs
print_status "Generating TypeScript client..."
mvn org.openapitools:openapi-generator-maven-plugin:generate@generate-typescript-client

print_status "Generating Java client..."
mvn org.openapitools:openapi-generator-maven-plugin:generate@generate-java-client

# Format code using Spotless
print_status "Formatting code with Spotless..."
mvn spotless:apply

# Run code quality checks
print_status "Running code quality checks..."
mvn checkstyle:check pmd:check spotbugs:check

print_success "✅ API generation completed successfully!"

# Display generated files
echo ""
print_status "Generated files:"
echo "📄 OpenAPI Spec: target/openapi.json"
echo "📁 TypeScript Client: generated/typescript-client/"
echo "📁 Java Client: generated/java-client/"

# Create package for TypeScript client
if [ -d "generated/typescript-client" ]; then
    print_status "Creating TypeScript client package..."
    cd generated/typescript-client
    if command -v npm &> /dev/null; then
        npm install
        npm run build 2>/dev/null || true
        print_success "TypeScript client package ready"
    else
        print_warning "npm not found, skipping TypeScript client build"
    fi
    cd ../..
fi

echo ""
print_success "🎉 All done! Your API documentation and clients are ready to use."
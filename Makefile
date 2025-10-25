# Smart Task Management - Makefile
# Provides convenient commands for development, testing, and deployment

.PHONY: help clean build test format check generate-api docs dev prod fast quality

# Default target
.DEFAULT_GOAL := help

# Colors for output
BLUE := \033[36m
GREEN := \033[32m
YELLOW := \033[33m
RED := \033[31m
RESET := \033[0m

## Display this help message
help:
	@echo "$(BLUE)Smart Task Management - Available Commands$(RESET)"
	@echo ""
	@echo "$(GREEN)Development:$(RESET)"
	@echo "  make dev          - Run application in development mode"
	@echo "  make build        - Build the application"
	@echo "  make test         - Run all tests"
	@echo "  make clean        - Clean build artifacts"
	@echo ""
	@echo "$(GREEN)Code Quality:$(RESET)"
	@echo "  make format       - Format code using Spotless"
	@echo "  make check        - Run code quality checks"
	@echo "  make quality      - Run comprehensive quality analysis"
	@echo ""
	@echo "$(GREEN)API Documentation:$(RESET)"
	@echo "  make generate-api - Generate OpenAPI spec and client SDKs"
	@echo "  make docs         - Generate and serve API documentation"
	@echo ""
	@echo "$(GREEN)Deployment:$(RESET)"
	@echo "  make prod         - Build for production"
	@echo "  make fast         - Fast build (skip tests and checks)"
	@echo ""
	@echo "$(GREEN)Utilities:$(RESET)"
	@echo "  make deps         - Download dependencies"
	@echo "  make package      - Create distribution package"

## Clean build artifacts
clean:
	@echo "$(YELLOW)Cleaning build artifacts...$(RESET)"
	@./mvnw clean
	@rm -rf generated/
	@echo "$(GREEN)✅ Clean completed$(RESET)"

## Download dependencies
deps:
	@echo "$(YELLOW)Downloading dependencies...$(RESET)"
	@./mvnw dependency:resolve
	@echo "$(GREEN)✅ Dependencies downloaded$(RESET)"

## Build the application
build: clean
	@echo "$(YELLOW)Building application...$(RESET)"
	@./mvnw compile
	@echo "$(GREEN)✅ Build completed$(RESET)"

## Run all tests
test:
	@echo "$(YELLOW)Running tests...$(RESET)"
	@./mvnw test
	@echo "$(GREEN)✅ Tests completed$(RESET)"

## Format code using Spotless
format:
	@echo "$(YELLOW)Formatting code with Spotless...$(RESET)"
	@./mvnw spotless:apply
	@echo "$(GREEN)✅ Code formatting completed$(RESET)"

## Run code quality checks
check:
	@echo "$(YELLOW)Running code quality checks...$(RESET)"
	@./format-and-check.sh
	@echo "$(GREEN)✅ Quality checks completed$(RESET)"

## Run comprehensive quality analysis
quality:
	@echo "$(YELLOW)Running comprehensive quality analysis...$(RESET)"
	@./mvnw clean compile -Pquality
	@./mvnw test jacoco:report -Pquality
	@./mvnw checkstyle:check pmd:check spotbugs:check
	@echo "$(GREEN)✅ Quality analysis completed$(RESET)"
	@echo "$(BLUE)Reports available in target/site/$(RESET)"

## Generate OpenAPI specification and client SDKs
generate-api:
	@echo "$(YELLOW)Generating OpenAPI specification and clients...$(RESET)"
	@./generate-api.sh
	@echo "$(GREEN)✅ API generation completed$(RESET)"

## Generate and serve API documentation
docs: generate-api
	@echo "$(YELLOW)Starting documentation server...$(RESET)"
	@echo "$(BLUE)API Documentation available at: http://localhost:8080/swagger-ui.html$(RESET)"
	@./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=openapi-gen"

## Run application in development mode
dev:
	@echo "$(YELLOW)Starting application in development mode...$(RESET)"
	@echo "$(BLUE)Application will be available at: http://localhost:8080$(RESET)"
	@./mvnw spring-boot:run -Pdev

## Build for production
prod: clean format check
	@echo "$(YELLOW)Building for production...$(RESET)"
	@./mvnw package -Pprod
	@echo "$(GREEN)✅ Production build completed$(RESET)"

## Fast build (skip tests and checks)
fast:
	@echo "$(YELLOW)Running fast build...$(RESET)"
	@./mvnw package -Pfast
	@echo "$(GREEN)✅ Fast build completed$(RESET)"

## Create distribution package
package: prod
	@echo "$(YELLOW)Creating distribution package...$(RESET)"
	@mkdir -p dist/
	@cp target/*.jar dist/
	@cp -r generated/ dist/ 2>/dev/null || true
	@echo "$(GREEN)✅ Distribution package created in dist/$(RESET)"

## Install Git hooks for code quality
install-hooks:
	@echo "$(YELLOW)Installing Git hooks...$(RESET)"
	@echo '#!/bin/bash\nmake format check' > .git/hooks/pre-commit
	@chmod +x .git/hooks/pre-commit
	@echo "$(GREEN)✅ Git hooks installed$(RESET)"

## Run security scan
security:
	@echo "$(YELLOW)Running security scan...$(RESET)"
	@./mvnw org.owasp:dependency-check-maven:check
	@echo "$(GREEN)✅ Security scan completed$(RESET)"

## Generate project reports
reports:
	@echo "$(YELLOW)Generating project reports...$(RESET)"
	@./mvnw site
	@echo "$(GREEN)✅ Reports generated in target/site/$(RESET)"

## Validate project structure
validate:
	@echo "$(YELLOW)Validating project structure...$(RESET)"
	@./mvnw validate
	@echo "$(GREEN)✅ Project validation completed$(RESET)"
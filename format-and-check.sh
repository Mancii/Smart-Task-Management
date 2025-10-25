#!/bin/bash

# Smart Task Management Code Quality Script
# This script formats code and runs quality checks similar to Spotless

set -e

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

# Parse command line arguments
FIX_MODE=false
SKIP_TESTS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --fix)
            FIX_MODE=true
            shift
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --fix         Apply fixes automatically"
            echo "  --skip-tests  Skip running tests"
            echo "  -h, --help    Show this help message"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

echo "🔧 Starting code quality checks and formatting..."

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    exit 1
fi

# Step 1: Spotless formatting (Java formatting disabled to preserve original style)
if [ "$FIX_MODE" = true ]; then
    print_status "Applying Spotless formatting (Java code formatting disabled)..."
    mvn spotless:apply
    print_success "Non-Java formatting applied - Java code style completely preserved"
else
    print_status "Checking Spotless formatting (Java formatting disabled)..."
    if mvn spotless:check; then
        print_success "Formatting check passed"
    else
        print_warning "Non-Java formatting issues found (Markdown, POM, etc.)"
        print_warning "Note: Java code formatting is disabled to preserve your original style"
    fi
fi

# Step 2: Compile the project
print_status "Compiling project..."
mvn clean compile

# Step 3: Run Checkstyle
print_status "Running Checkstyle analysis..."
if mvn checkstyle:check; then
    print_success "Checkstyle passed"
else
    print_warning "Checkstyle issues found. Check target/checkstyle-result.xml for details."
fi

# Step 4: Run PMD
print_status "Running PMD analysis..."
if mvn pmd:check; then
    print_success "PMD analysis passed"
else
    print_warning "PMD issues found. Check target/pmd.xml for details."
fi

# Step 5: Run SpotBugs
print_status "Running SpotBugs analysis..."
if mvn spotbugs:check; then
    print_success "SpotBugs analysis passed"
else
    print_warning "SpotBugs issues found. Check target/spotbugsXml.xml for details."
fi

# Step 6: Run tests (if not skipped)
if [ "$SKIP_TESTS" = false ]; then
    print_status "Running tests..."
    if mvn test; then
        print_success "All tests passed"
    else
        print_error "Some tests failed"
        exit 1
    fi
else
    print_warning "Skipping tests as requested"
fi

# Step 7: Generate reports
print_status "Generating quality reports..."
mvn site -DgenerateReports=true 2>/dev/null || print_warning "Failed to generate some reports"

# Summary
echo ""
print_success "🎉 Code quality check completed!"
echo ""
print_status "Generated reports:"
echo "📊 Checkstyle: target/site/checkstyle.html"
echo "📊 PMD: target/site/pmd.html"
echo "📊 SpotBugs: target/site/spotbugs.html"
echo "📊 Test Results: target/site/surefire-report.html"

# Check if any issues were found
if [ -f "target/checkstyle-result.xml" ] && grep -q "error" target/checkstyle-result.xml; then
    print_warning "Checkstyle errors found - review target/site/checkstyle.html"
fi

if [ -f "target/pmd.xml" ] && grep -q "violation" target/pmd.xml; then
    print_warning "PMD violations found - review target/site/pmd.html"
fi

if [ -f "target/spotbugsXml.xml" ] && grep -q "BugInstance" target/spotbugsXml.xml; then
    print_warning "SpotBugs issues found - review target/site/spotbugs.html"
fi

print_success "✅ Quality check process completed successfully!"
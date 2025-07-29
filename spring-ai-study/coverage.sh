#!/bin/bash
# Coverage Report Generation Script for Unix/Linux/macOS
# This script runs tests and generates JaCoCo coverage reports

echo "========================================"
echo "Spring AI Study - Coverage Report Tool"
echo "========================================"

show_help() {
    echo "Usage:"
    echo "  ./coverage.sh [command] [profile]"
    echo ""
    echo "Commands:"
    echo "  unit        - Run unit tests only with coverage"
    echo "  integration - Run integration tests only with coverage"
    echo "  all         - Run all tests with coverage (default)"
    echo "  report      - Generate coverage report without running tests"
    echo "  clean       - Clean previous coverage data"
    echo "  open        - Open coverage report in browser"
    echo "  help        - Show this help message"
    echo ""
    echo "Profiles:"
    echo "  dev         - Development profile (70% coverage threshold)"
    echo "  ci          - CI/CD profile (85% coverage threshold)"
    echo "  (default)   - Standard profile (80% coverage threshold)"
    echo ""
    echo "Examples:"
    echo "  ./coverage.sh all dev"
    echo "  ./coverage.sh unit"
    echo "  ./coverage.sh open"
}

COMMAND=${1:-all}
PROFILE=$2

if [ "$COMMAND" = "help" ]; then
    show_help
    exit 0
fi

if [ -n "$PROFILE" ]; then
    MAVEN_PROFILE="-P$PROFILE"
else
    MAVEN_PROFILE=""
fi

echo "Command: $COMMAND"
if [ -n "$PROFILE" ]; then
    echo "Profile: $PROFILE"
fi
echo ""

case $COMMAND in
    "clean")
        echo "Cleaning previous coverage data..."
        mvn clean $MAVEN_PROFILE
        ;;
    "unit")
        echo "Running unit tests with coverage..."
        mvn clean test jacoco:report $MAVEN_PROFILE
        show_results
        ;;
    "integration")
        echo "Running integration tests with coverage..."
        mvn clean verify -DskipUnitTests=true $MAVEN_PROFILE
        show_results
        ;;
    "all")
        echo "Running all tests with coverage..."
        mvn clean verify $MAVEN_PROFILE
        show_results
        ;;
    "report")
        echo "Generating coverage report..."
        mvn jacoco:report $MAVEN_PROFILE
        show_results
        ;;
    "open")
        echo "Opening coverage report..."
        if [ -f "target/site/jacoco/index.html" ]; then
            if command -v xdg-open > /dev/null; then
                xdg-open target/site/jacoco/index.html
            elif command -v open > /dev/null; then
                open target/site/jacoco/index.html
            else
                echo "Please open target/site/jacoco/index.html in your browser"
            fi
        elif [ -f "target/site/jacoco-merged/index.html" ]; then
            if command -v xdg-open > /dev/null; then
                xdg-open target/site/jacoco-merged/index.html
            elif command -v open > /dev/null; then
                open target/site/jacoco-merged/index.html
            else
                echo "Please open target/site/jacoco-merged/index.html in your browser"
            fi
        else
            echo "Coverage report not found. Please run tests first."
            echo "Use: ./coverage.sh all"
        fi
        ;;
    *)
        echo "Unknown command: $COMMAND"
        echo "Use './coverage.sh help' for usage information."
        exit 1
        ;;
esac

show_results() {
    echo ""
    echo "========================================"
    echo "Coverage Report Generation Complete"
    echo "========================================"
    echo ""
    echo "Reports generated in:"
    if [ -f "target/site/jacoco/index.html" ]; then
        echo "  Unit Tests: target/site/jacoco/index.html"
    fi
    if [ -f "target/site/jacoco-it/index.html" ]; then
        echo "  Integration Tests: target/site/jacoco-it/index.html"
    fi
    if [ -f "target/site/jacoco-merged/index.html" ]; then
        echo "  Merged Report: target/site/jacoco-merged/index.html"
    fi
    echo ""
    echo "To open the report in your browser:"
    echo "  ./coverage.sh open"
    echo ""
}
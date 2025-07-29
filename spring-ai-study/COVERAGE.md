# Test Coverage Configuration

This document describes the JaCoCo test coverage configuration for the Spring AI Study project.

## Overview

The project uses JaCoCo (Java Code Coverage) to measure and report test coverage. The configuration includes:

- Unit test coverage
- Integration test coverage
- Merged coverage reports
- Coverage threshold enforcement
- Multiple reporting formats (HTML, XML, CSV)

## Configuration

### Maven Plugin Configuration

The JaCoCo plugin is configured in `pom.xml` with the following features:

- **Version**: 0.8.11 (latest stable)
- **Coverage Threshold**: 80% (configurable via profiles)
- **Exclusions**: Main application class, DTOs, and configuration properties
- **Reports**: HTML, XML, and CSV formats

### Coverage Thresholds

- **Default**: 80% instruction and branch coverage
- **Development Profile (`dev`)**: 70% coverage threshold
- **CI/CD Profile (`ci`)**: 85% coverage threshold

### Excluded Classes

The following classes are excluded from coverage requirements:

- `org.miao.SpringAiApplication` - Main application class
- `org.miao.dto.**` - Data Transfer Objects (simple data classes)
- `org.miao.config.**Properties` - Configuration property classes

## Usage

### Running Tests with Coverage

#### Option 1: Using Maven Commands

```bash
# Run all tests with coverage
mvn clean verify

# Run only unit tests with coverage
mvn clean test jacoco:report

# Run only integration tests with coverage
mvn clean verify -DskipUnitTests=true

# Generate coverage report without running tests
mvn jacoco:report
```

#### Option 2: Using Coverage Scripts

**Windows:**
```cmd
# Run all tests with coverage
coverage.bat all

# Run unit tests only
coverage.bat unit

# Run integration tests only
coverage.bat integration

# Open coverage report in browser
coverage.bat open

# Show help
coverage.bat help
```

**Unix/Linux/macOS:**
```bash
# Make script executable (first time only)
chmod +x coverage.sh

# Run all tests with coverage
./coverage.sh all

# Run unit tests only
./coverage.sh unit

# Run integration tests only
./coverage.sh integration

# Open coverage report in browser
./coverage.sh open

# Show help
./coverage.sh help
```

### Using Profiles

You can use different profiles to adjust coverage thresholds:

```bash
# Development profile (70% threshold)
mvn clean verify -Pdev
# or
coverage.bat all dev

# CI/CD profile (85% threshold)
mvn clean verify -Pci
# or
coverage.bat all ci
```

## Coverage Reports

After running tests, coverage reports are generated in the following locations:

- **Unit Test Coverage**: `target/site/jacoco/index.html`
- **Integration Test Coverage**: `target/site/jacoco-it/index.html`
- **Merged Coverage**: `target/site/jacoco-merged/index.html`

### Report Formats

JaCoCo generates reports in multiple formats:

1. **HTML**: Interactive web-based reports with drill-down capabilities
2. **XML**: Machine-readable format for CI/CD integration
3. **CSV**: Spreadsheet-compatible format for analysis

## Coverage Metrics

JaCoCo tracks several coverage metrics:

- **Instruction Coverage**: Percentage of bytecode instructions executed
- **Branch Coverage**: Percentage of branches (if/else, switch) executed
- **Line Coverage**: Percentage of source code lines executed
- **Complexity Coverage**: Cyclomatic complexity coverage
- **Method Coverage**: Percentage of methods executed
- **Class Coverage**: Percentage of classes with at least one method executed

## Integration with IDEs

### IntelliJ IDEA

1. Install the JaCoCo plugin (if not already installed)
2. Run tests with coverage: `Run > Run with Coverage`
3. View coverage in the editor with highlighted lines

### Eclipse

1. Install EclEmma (JaCoCo integration for Eclipse)
2. Right-click project > Coverage As > JUnit Test
3. View coverage in the Coverage view

### Visual Studio Code

1. Install the "Coverage Gutters" extension
2. Run tests with coverage using Maven
3. Use Command Palette: "Coverage Gutters: Display Coverage"

## CI/CD Integration

### GitHub Actions Example

```yaml
- name: Run tests with coverage
  run: mvn clean verify -Pci

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
  with:
    file: target/site/jacoco-merged/jacoco.xml
```

### Jenkins Example

```groovy
stage('Test Coverage') {
    steps {
        sh 'mvn clean verify -Pci'
        publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: 'target/site/jacoco-merged',
            reportFiles: 'index.html',
            reportName: 'Coverage Report'
        ])
    }
}
```

## Troubleshooting

### Common Issues

1. **Coverage reports not generated**
   - Ensure tests are running successfully
   - Check that JaCoCo agent is properly attached
   - Verify Maven Surefire/Failsafe plugin configuration

2. **Low coverage warnings**
   - Review excluded classes configuration
   - Add more unit tests for uncovered code
   - Consider adjusting coverage thresholds for development

3. **Build fails due to coverage threshold**
   - Use development profile: `mvn clean verify -Pdev`
   - Add tests to increase coverage
   - Review and adjust threshold settings

### Debug Mode

To debug JaCoCo configuration, add the following to your Maven command:

```bash
mvn clean verify -X -Djacoco.debug=true
```

## Best Practices

1. **Aim for high coverage** but focus on meaningful tests
2. **Exclude generated code** and simple data classes
3. **Use different thresholds** for different environments
4. **Review coverage reports** regularly to identify gaps
5. **Integrate coverage checks** into your CI/CD pipeline
6. **Don't chase 100% coverage** - focus on critical business logic

## Configuration Files

- **Maven Configuration**: `pom.xml` (JaCoCo plugin section)
- **Coverage Scripts**: `coverage.bat` (Windows), `coverage.sh` (Unix/Linux/macOS)
- **Documentation**: This file (`COVERAGE.md`)
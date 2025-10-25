# OpenAPI Generator & Code Quality Setup

This document describes the OpenAPI generator and code quality tools integrated into the Smart Task Management project, similar to Spotless functionality.

## 🚀 Quick Start

```bash
# Generate API documentation and client SDKs
make generate-api

# Format code and run quality checks
make check

# Run comprehensive quality analysis
make quality

# Start development server with API docs
make docs
```

## 📋 Features

### OpenAPI Generation

- **Automatic API Documentation**: Generates comprehensive OpenAPI 3.0 specification
- **Client SDK Generation**: Creates TypeScript and Java client libraries
- **Interactive Documentation**: Swagger UI with try-it-out functionality
- **Security Schemes**: JWT Bearer token authentication

### Code Quality (Spotless-like functionality)

- **Code Formatting**: Google Java Format with custom rules
- **Import Organization**: Automatic import sorting and cleanup
- **Static Analysis**: Checkstyle, PMD, and SpotBugs integration
- **Test Coverage**: JaCoCo code coverage reports

## 🛠 Configuration

### Maven Plugins

#### Spotless Plugin

```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.43.0</version>
    <configuration>
        <java>
            <googleJavaFormat>
                <version>1.19.2</version>
                <style>GOOGLE</style>
            </googleJavaFormat>
            <importOrder>
                <order>java,javax,jakarta,org,com,</order>
            </importOrder>
            <removeUnusedImports />
            <formatAnnotations />
        </java>
    </configuration>
</plugin>
```

#### OpenAPI Generator Plugin

```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.8.0</version>
    <executions>
        <execution>
            <id>generate-typescript-client</id>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <generatorName>typescript-axios</generatorName>
                <output>${project.basedir}/generated/typescript-client</output>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Maven Profiles

|    Profile    |        Purpose        |                Usage                |
|---------------|-----------------------|-------------------------------------|
| `dev`         | Development (default) | `mvn spring-boot:run -Pdev`         |
| `prod`        | Production build      | `mvn package -Pprod`                |
| `quality`     | Quality analysis      | `mvn test -Pquality`                |
| `openapi-gen` | API generation        | `mvn spring-boot:run -Popenapi-gen` |
| `fast`        | Skip tests/checks     | `mvn package -Pfast`                |

## 📖 Usage Guide

### 1. Generate API Documentation

```bash
# Using script
./generate-api.sh

# Using Maven
mvn clean compile
mvn spring-boot:run -Popenapi-gen &
curl http://localhost:8080/v3/api-docs > target/openapi.json
mvn org.openapitools:openapi-generator-maven-plugin:generate
```

### 2. Code Formatting

```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply

# Using script
./format-and-check.sh --fix
```

### 3. Quality Checks

```bash
# Run all quality checks
./format-and-check.sh

# Individual checks
mvn checkstyle:check
mvn pmd:check
mvn spotbugs:check
```

### 4. Client SDK Generation

#### TypeScript Client

```bash
# Generated in: generated/typescript-client/
npm install ./generated/typescript-client
```

```typescript
import { DefaultApi, Configuration } from 'smart-task-management-client';

const config = new Configuration({
    basePath: 'http://localhost:8080',
    accessToken: 'your-jwt-token'
});

const api = new DefaultApi(config);
const users = await api.getAllUsers();
```

#### Java Client

```xml
<dependency>
    <groupId>com.task.client</groupId>
    <artifactId>smart-task-management-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
ApiClient client = new ApiClient();
client.setBasePath("http://localhost:8080");
client.setBearerToken("your-jwt-token");

UserApi userApi = new UserApi(client);
List<UserDto> users = userApi.getAllUsers(0, 10, new String[]{"id", "asc"});
```

## 🔧 Customization

### Adding New Generators

1. **Add execution to POM**:

```xml
<execution>
    <id>generate-python-client</id>
    <goals>
        <goal>generate</goal>
    </goals>
    <configuration>
        <generatorName>python</generatorName>
        <output>${project.basedir}/generated/python-client</output>
    </configuration>
</execution>
```

2. **Update generation script**:

```bash
# Add to generate-api.sh
print_status "Generating Python client..."
mvn org.openapitools:openapi-generator-maven-plugin:generate@generate-python-client
```

### Custom Code Quality Rules

1. **Checkstyle**: Create `checkstyle.xml` in project root
2. **PMD**: Add custom rulesets in `pmd-ruleset.xml`
3. **SpotBugs**: Configure exclusions in `spotbugs-exclude.xml`

### OpenAPI Customization

```java
@Configuration
public class CustomOpenApiConfig {
    
    @Bean
    public OpenApiCustomizer customizer() {
        return openApi -> {
            // Add custom examples
            // Modify security schemes
            // Add global responses
        };
    }
}
```

## 📊 Reports and Output

### Generated Files

```
target/
├── openapi.json              # OpenAPI specification
├── site/
│   ├── checkstyle.html      # Checkstyle report
│   ├── pmd.html             # PMD report
│   ├── spotbugs.html        # SpotBugs report
│   └── jacoco/              # Coverage reports
└── ...

generated/
├── typescript-client/        # TypeScript SDK
├── java-client/             # Java SDK
└── ...
```

### Quality Metrics

- **Code Coverage**: Target > 80%
- **Checkstyle**: Google Java Style compliance
- **PMD**: Best practices and code quality
- **SpotBugs**: Bug pattern detection

## 🔄 CI/CD Integration

### GitHub Actions

```yaml
name: Quality Check
on: [push, pull_request]
jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run Quality Checks
        run: make check
      - name: Generate API
        run: make generate-api
```

### Pre-commit Hooks

```bash
# Install hooks
make install-hooks

# Manual setup
echo '#!/bin/bash\nmake format check' > .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

## 🚨 Troubleshooting

### Common Issues

1. **OpenAPI Generation Fails**
   - Ensure application starts successfully
   - Check port 8080 is available
   - Verify database configuration
2. **Spotless Formatting Issues**
   - Run `mvn spotless:apply` to fix
   - Check Java version compatibility
   - Verify file encoding (UTF-8)
3. **Quality Check Failures**
   - Review generated reports in `target/site/`
   - Fix issues incrementally
   - Use `--skip-tests` for quick builds

### Debug Mode

```bash
# Enable debug logging
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
mvn spring-boot:run -Popenapi-gen
```

## 📚 Additional Resources

- [OpenAPI Generator Documentation](https://openapi-generator.tech/)
- [Spotless Plugin Guide](https://github.com/diffplug/spotless/tree/main/plugin-maven)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [Maven Profiles Guide](https://maven.apache.org/guides/introduction/introduction-to-profiles.html)

## 🤝 Contributing

1. Follow the established code formatting rules
2. Add OpenAPI annotations to new endpoints
3. Update documentation for new features
4. Ensure all quality checks pass before committing


# Audition API

[![Build](https://github.com/Vijay-Duke/audition-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Vijay-Duke/audition-api/actions/workflows/ci.yml)
[![CodeQL](https://github.com/Vijay-Duke/audition-api/actions/workflows/codeql.yml/badge.svg)](https://github.com/Vijay-Duke/audition-api/actions/workflows/codeql.yml)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
![Profile Views](https://komarev.com/ghpvc/?username=Vijay-Duke&repo=audition-api)

## Reports
- [API Documentation](https://vijay-duke.github.io/audition-api/reports/openapi/index.html)
- [Test Results](https://vijay-duke.github.io/audition-api/reports/tests/test/index.html)
- [Code Coverage](https://vijay-duke.github.io/audition-api/reports/jacoco/test/html/index.html)
- [Checkstyle](https://vijay-duke.github.io/audition-api/reports/checkstyle/main.html)
- [PMD](https://vijay-duke.github.io/audition-api/reports/pmd/main.html)

---

The purpose of this Spring Boot application is to test general knowledge of SpringBoot, Java, Gradle etc. It is created for hiring needs of our company but can be used for other purposes.

## Overarching expectations & Assessment areas

<pre>
This is not a university test. 
This is meant to be used for job applications and MUST showcase your full skillset. 
<b>As such, PRODUCTION-READY code must be written and submitted. </b> 
</pre>

- clean, easy to understand code
- good code structures
- Proper code encapsulation
- unit tests with minimum 80% coverage.
- A Working application to be submitted.
- Observability. Does the application contain Logging, Tracing and Metrics instrumentation?
- Input validation.
- Proper error handling.
- Ability to use and configure rest template. We allow for half-setup object mapper and rest template
- Not all information in the Application is perfect. It is expected that a person would figure these out and correct.
  
## Getting Started

### Prerequisite tooling

- Any Springboot/Java IDE. Ideally IntelliJIdea.
- Java 17
- Gradle 8
  
### Prerequisite knowledge

- Java
- SpringBoot
- Gradle
- Junit

### Importing Google Java codestyle into INtelliJ

```
- Go to IntelliJ Settings
- Search for "Code Style"
- Click on the "Settings" icon next to the Scheme dropdown
- Choose "Import -> IntelliJ Idea code style XML
- Pick the file "google_java_code_style.xml" from root directory of the application
__Optional__
- Search for "Actions on Save"
    - Check "Reformat Code" and "Organise Imports"
```

---
**NOTE** -
It is  highly recommended that the application be loaded and started up to avoid any issues.

---

## Audition Application information

This section provides information on the application and what the needs to be completed as part of the audition application.

The audition consists of multiple TODO statements scattered throughout the codebase. The applicants are expected to:

- Complete all the TODO statements.
- Add unit tests where applicants believe it to be necessary.
- Make sure that all code quality check are completed.
- Gradle build completes sucessfully.
- Make sure the application if functional.

## Submission process
Applicants need to do the following to submit their work: 
- Clone this repository
- Complete their work and zip up the working application. 
- Applicants then need to send the ZIP archive to the email of the recruiting manager. This email be communicated to the applicant during the recruitment process. 

  
---
## Additional Information based on the implementation

This section MUST be completed by applicants. It allows applicants to showcase their view on how an application can/should be documented.
Applicants can choose to do this in a separate markdown file that needs to be included when the code is committed.

---

## Implementation Documentation

### Architecture Overview

The application follows a **layered architecture** pattern:

```
Controller (AuditionController)
    ↓
Service (AuditionService)
    ↓
Integration Client (AuditionIntegrationClient)
    ↓
External API (JSONPlaceholder)
```

### API Endpoints

All endpoints are versioned under `/api/v1/`:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/posts` | Retrieve posts with optional filtering, pagination, and sorting |
| GET | `/api/v1/posts/{id}` | Get a single post by ID |
| GET | `/api/v1/posts/{id}?include=comments` | Get a post with embedded comments |
| GET | `/api/v1/posts/{postId}/comments` | Get all comments for a post |

#### Query Parameters for `/posts`

| Parameter | Type | Description |
|-----------|------|-------------|
| `userId` | Integer | Filter posts by user ID |
| `titleContains` | String | Filter posts where title contains this text |
| `page` | Integer | Page number (1-indexed, requires `size`) |
| `size` | Integer | Page size, max 100 (requires `page`) |
| `sort` | String | Sort field: `id`, `userId`, or `title` |
| `order` | String | Sort order: `asc` or `desc` |

### Key Design Decisions

#### 1. Immutable Models with Lombok
- Used `@Value` and `@Builder` for immutable DTOs (`AuditionPost`, `Comment`, `PostSearchCriteria`)
- Ensures thread-safety and prevents accidental state mutations

#### 2. Externalized Configuration
- All API settings externalized via `@ConfigurationProperties` (`AuditionApiProperties`)
- Timeouts, base URLs, and circuit breaker names configurable in `application.yml`

#### 3. Resilience Patterns (Resilience4j)
- **Circuit Breaker**: Prevents cascading failures when upstream API is down
- **Retry**: Automatic retry with exponential backoff for transient failures (5xx, connection errors)
- **Timeout**: Configurable timeout limits

#### 4. Error Handling
- RFC 7807 `ProblemDetail` responses for all errors
- Centralized exception handling via `@ControllerAdvice`
- Custom `SystemException` with factory methods for common HTTP status codes

#### 5. Input Validation
- JSR-380 Bean Validation annotations (`@Positive`, `@Pattern`, `@Min`)
- Custom validator (`@ValidPostSearchCriteria`) for cross-field validation

### Observability

#### Logging
- SLF4J with Logback
- Structured JSON logging for production (`logback-spring.xml`)
- Request/response logging via `RestTemplateLoggingInterceptor`
  - Note: `AuditionLogger` was removed in favor of `RestTemplateLoggingInterceptor` which integrates directly with RestTemplate as an interceptor

To see outgoing HTTP request/response logs (to JSONPlaceholder), enable DEBUG level:
```bash
./gradlew bootRun --args='--logging.level.com.audition.configuration.RestTemplateLoggingInterceptor=DEBUG'
```

Or add to `application.yml`:
```yaml
logging:
  level:
    com.audition.configuration.RestTemplateLoggingInterceptor: DEBUG
```

#### Tracing
- Micrometer Tracing with Brave bridge
- **W3C Trace Context** (`traceparent`, `tracestate` headers) as primary propagation
- B3 headers supported for backward compatibility
- Trace/span IDs propagated via `ResponseHeaderInjector`
- 100% sampling rate (configurable)

#### Metrics
- Spring Boot Actuator endpoints
- Prometheus metrics endpoint at `/actuator/prometheus`
- Health check at `/actuator/health`

### Running the Application

```bash
# Build and run tests
./gradlew clean build

# Run the application (default profile)
./gradlew bootRun

# Run with development profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run with production profile
./gradlew bootRun --args='--spring.profiles.active=prod'

# Run with Docker
docker build -t audition-api .
docker run -p 8080:8080 audition-api
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod audition-api
```

### Environment Profiles

| Profile | Description |
|---------|-------------|
| `default` | Base configuration, console logging |
| `dev` | Development: verbose logging, relaxed timeouts, all actuator endpoints |
| `prod` | Production: JSON logging, strict circuit breaker, compression enabled |

### API Documentation

OpenAPI/Swagger UI available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Test Coverage

Run tests with coverage report:
```bash
./gradlew test jacocoTestReport
```

Coverage report available at: `build/reports/jacoco/test/html/index.html`

### Code Quality

Static analysis enabled via:
- **Checkstyle**: Google Java Style Guide
- **PMD**: Custom ruleset for bug detection
- **SpotBugs**: FindBugs successor for bytecode analysis 

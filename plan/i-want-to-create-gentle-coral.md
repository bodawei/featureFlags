# Spring Boot Feature Flags Skeleton

## Context
Create a minimal Spring Boot web application skeleton in the existing empty git repo at `/Users/djb/hunting/featureFlags`. The app is the foundation for a feature flags service; the user will add flag logic themselves. Requirements: Java 26 (target, released March 2026), Maven, Spring Boot 3.5.0 (latest stable, released 2025-05-22). JDK 26 must be installed to compile Java 26 source with `--release 26`.

## Version Choices
| Component | Version |
|---|---|
| Spring Boot parent BOM | `4.0.6` |
| Java source/target | `26` |
| Embedded server | Tomcat (bundled in `spring-boot-starter-web`) |

## Files to Create

### Directory structure
```
featureFlags/
├── pom.xml
├── .gitignore
└── src/main/
    ├── java/com/featureflags/
    │   ├── FeatureFlagsApplication.java
    │   └── controller/HealthController.java
    └── resources/
        └── application.properties
```

### 1. `pom.xml`
- Parent: `spring-boot-starter-parent:4.0.6`
- `<java.version>26</java.version>` (Spring Boot parent uses this property to set `maven-compiler-plugin` `--release`)
- Dependencies: `spring-boot-starter-web`, `spring-boot-starter-actuator`
- Plugin: `spring-boot-maven-plugin` (produces executable fat JAR on `mvn package`)

### 2. `src/main/java/com/featureflags/FeatureFlagsApplication.java`
- `@SpringBootApplication` entry point; `SpringApplication.run(...)` in `main()`
- Package `com.featureflags` — component scan root

### 3. `src/main/java/com/featureflags/controller/HealthController.java`
- `@RestController` with `GET /health` returning a `record HealthResponse(String status, String message)`
- Response: `{"status":"ok","message":"feature-flags service is running"}`
- Separate from Actuator's `/actuator/health`

### 4. `src/main/resources/application.properties`
```properties
spring.application.name=feature-flags
server.port=8080
management.endpoints.web.exposure.include=health,info
```
Actuator exposes only `health` and `info` over HTTP (safe read-only endpoints).

### 5. `.gitignore`
Standard Java/Maven/IDE entries: `target/`, `*.class`, `.idea/`, `.vscode/`, `.DS_Store`, `*.log`

## Verification
1. `mvn clean package` → `BUILD SUCCESS`; fat JAR at `target/feature-flags-0.1.0-SNAPSHOT.jar`
2. `java -jar target/feature-flags-0.1.0-SNAPSHOT.jar` → `Started FeatureFlagsApplication in X.XXX seconds`
3. `curl http://localhost:8080/health` → `{"status":"ok","message":"feature-flags service is running"}`
4. `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`

# Build & Development Guide

## Build Commands
```bash
# Build without tests
./gradlew clean build -x test

# Build with tests
./gradlew clean build

# Run tests
./gradlew test
```

## Local Execution
```bash
# Run with local profile (requires local DB and environment variables)
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Docker Operations
```bash
# Build Docker image locally
make build

# Build and push multi-platform images (linux/amd64, linux/arm64)
make buildx-and-push APP_TAG=v1.0.0

# Deploy to servers (green -> blue sequence)
make deploy
```

## Environment Configuration
Required environment variables (refer to `application.yml`):
*   `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`
*   `KAKAO_REDIRECT_URI` (OAuth2 callback)
*   JWT secrets must be overridden in production environments.

### Active Profiles
*   `local`: For local development and debugging.
*   `prod`: For production deployment.
*   `test`: For CI/CD and local test execution.

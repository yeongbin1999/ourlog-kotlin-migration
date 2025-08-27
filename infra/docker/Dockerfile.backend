# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY ../../backend/build.gradle ../../backend/settings.gradle ./
COPY ../../backend/gradle ./gradle
RUN mkdir -p src && echo > src/empty
RUN ./gradlew build --no-daemon || true
COPY ../../backend/ ./backend/
WORKDIR /app/backend
RUN ./gradlew clean build -x test -Pprod --no-daemon

# Run stage
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
# ------------------ Build Stage ------------------
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Gradle 래퍼/설정 먼저 복사 (캐시 효율)
COPY backend/gradlew backend/settings.gradle* backend/build.gradle* ./backend/
COPY backend/gradle ./backend/gradle

# gradlew 줄바꿈 정리 + 실행 권한
RUN sed -i 's/\r$//' ./backend/gradlew && chmod +x ./backend/gradlew

# 소스 복사
COPY backend/src ./backend/src

# .env 복사
COPY backend/.env ./backend/.env

# 빌드 (테스트 제외, prod 프로파일)
WORKDIR /app/backend
RUN ./gradlew clean build -x test -Pprod --no-daemon

# 실행할 JAR만 app.jar로 지정
RUN JAR="$(ls build/libs/*.jar | grep -v 'plain' | head -n 1)" \
 && cp "$JAR" /app/app.jar

# ------------------ Run Stage ------------------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# 빌드 결과물과 .env 복사
COPY --from=build /app/app.jar /app/app.jar
COPY --from=build /app/backend/.env .env

# JVM 컨테이너 환경 최적화
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

# Spring Boot 프로파일 지정 및 포트 노출
EXPOSE 8080

# JAR 실행 시 .env 사용 가능
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dspring.profiles.active=prod -jar /app/app.jar"]
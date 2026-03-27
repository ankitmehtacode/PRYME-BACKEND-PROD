# ==========================================
# STAGE 1: Builder (JAVA 21)
# Alpine for fast build — compilation doesn't care about runtime DNS.
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# THE WINDOWS FAILSAFE: Fixes CRLF issues if code is pulled from Windows Git
RUN apk add --no-cache dos2unix

# Copy Maven architecture first to leverage Docker layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Sanitize wrapper and resolve dependencies offline
RUN dos2unix mvnw && chmod +x mvnw

# Dependency failures must fail loudly — no || true suppression
RUN ./mvnw dependency:go-offline -B

# Copy source code and compile the Fat JAR
COPY src src
RUN ./mvnw clean package -DskipTests

# ==========================================
# STAGE 2: Secure Runtime (JAVA 21 + GLIBC)
# Ubuntu-based jammy eradicates Alpine/musl DNS packet-drop bug.
# ==========================================
FROM eclipse-temurin:21-jre-jammy

# Financial systems must operate in UTC at OS level
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# ZERO-TRUST: Create restricted user & group
RUN groupadd -r prymegroup && useradd -r -g prymegroup prymeuser

# Critical infrastructure directories
# /app/var/document-vault: persistent PDF storage (volume-map in docker-compose)
# /app/tmp: Tomcat spool directory (prevents large upload crashes)
RUN mkdir -p /app/var/document-vault \
    && mkdir -p /app/tmp \
    && chown -R prymeuser:prymegroup /app

# Extract compiled JAR from builder stage
COPY --from=builder /build/target/*.jar pryme-backend.jar
RUN chown prymeuser:prymegroup pryme-backend.jar

# Drop OS privileges permanently
USER prymeuser

EXPOSE 8080

# ==========================================
# KVM4 ENTRYPOINT (JAVA 21)
# MaxRAMPercentage=70.0: Leaves 30% for OS + Postgres co-located on KVM4.
# MaxMetaspaceSize=512m: Larger metaspace for expanded Java 21 class model.
# ==========================================
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=70.0", \
  "-XX:MaxMetaspaceSize=512m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Djava.io.tmpdir=/app/tmp", \
  "-jar", "pryme-backend.jar"]
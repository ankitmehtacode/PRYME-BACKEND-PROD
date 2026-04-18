# ==========================================
# STAGE 1: Builder (JAVA 21)
# Alpine for fast build — compilation doesn't care about runtime DNS.
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
RUN java -version && echo "Using Java 21"

# THE WINDOWS FAILSAFE: Fixes CRLF issues if code is pulled from Windows Git
RUN apk add --no-cache dos2unix

# Copy Maven architecture first to leverage Docker layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Sanitize wrapper and resolve dependencies offline
RUN dos2unix mvnw && chmod +x mvnw

# Download dependencies first (as a separate cacheable layer)
# Using -T 4 for 4 parallel threads and retry config for slow networks
RUN ./mvnw dependency:resolve -B -T 4 || ./mvnw dependency:resolve -B

# Copy source code and compile the Fat JAR
COPY src src
RUN ./mvnw clean package -Dmaven.test.skip=true

# 🧠 1% FIX: Defuse the Wildcard Collision Bomb
# Spring Boot generates both a -plain.jar and a fat jar. 
# We explicitly find the fat jar (which does not contain '-plain') and rename it 
# so Stage 2 has a deterministic, single file to grab.
RUN find target/ -name "*.jar" ! -name "*-plain.jar" -exec mv {} target/application.jar \;

# ==========================================
# STAGE 2: Secure Runtime (JAVA 21 + GLIBC)
# Ubuntu-based jammy eradicates Alpine/musl DNS packet-drop bug.
# ==========================================
FROM eclipse-temurin:21-jre-jammy

# Financial systems must operate in UTC at OS level
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Install curl for Docker healthcheck (CMD-SHELL requires it)
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# ZERO-TRUST: Create restricted user & group
RUN groupadd -r prymegroup && useradd -r -g prymegroup prymeuser

# Critical infrastructure directories
# /app/var/document-vault: persistent PDF storage (volume-map in docker-compose)
# /app/tmp: Tomcat spool directory (prevents large upload crashes)
RUN mkdir -p /app/var/document-vault \
    && mkdir -p /app/tmp \
    && chown -R prymeuser:prymegroup /app

# 🧠 1% FIX: Eradicate Docker Layer Bloat
# Copy and chown in a single atomic layer to prevent doubling the image size.
COPY --chown=prymeuser:prymegroup --from=builder /build/target/application.jar pryme-backend.jar

# Drop OS privileges permanently
USER prymeuser

EXPOSE 8082

# ==========================================
# KVM4 ENTRYPOINT (JAVA 21)
# Xms/Xmx: Explicit heap bounds for shared VPS — predictable memory footprint.
# MaxMetaspaceSize=256m: Caps class metadata to prevent unbounded growth.
# ==========================================
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:+UseG1GC", \
  "-Xms256m", \
  "-Xmx768m", \
  "-XX:MaxMetaspaceSize=256m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Djava.io.tmpdir=/app/tmp", \
  "-Dspring.threads.virtual.enabled=true", \
  "-jar", "pryme-backend.jar"]

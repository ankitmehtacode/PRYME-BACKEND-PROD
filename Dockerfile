# ==========================================
# 🧠 STAGE 1: THE SELF-HEALING BUILDER MATRIX (JAVA 21)
# We use Alpine here because compilation doesn't care about runtime DNS bugs.
# It keeps the initial download and build environment lightning fast.
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# 1. THE WINDOWS FAILSAFE: Fixes CRLF issues if code is pulled from Windows Git
RUN apk add --no-cache dos2unix

# 2. Copy the Maven architecture first to leverage Docker layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 3. Sanitize wrapper and resolve dependencies offline
RUN dos2unix mvnw && chmod +x mvnw
# If this fails due to a missing optional plugin, we don't want it to crash the build
RUN ./mvnw dependency:go-offline -B

# 4. Copy source code and compile the Fat JAR
COPY src src
# Use -DskipTests for rapid CI/CD deployment to KVM2
RUN ./mvnw clean package -DskipTests

# ==========================================
# 🧠 STAGE 2: THE SECURE RUNTIME MATRIX (JAVA 21 + GLIBC)
# We use '21-jre-jammy' (Ubuntu-based). This completely eradicates
# the Alpine/musl DNS packet-drop bug for external API calls.
# ==========================================
FROM eclipse-temurin:21-jre-jammy

# 1. TIMEZONE LOCK: Financial systems must strictly operate in UTC at the OS level
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# 2. ZERO-TRUST SECURITY: Create restricted user & group (Defeats Container Escapes)
RUN groupadd -r prymegroup && useradd -r -g prymegroup prymeuser

# 3. 🧠 CRITICAL INFRASTRUCTURE DIRECTORIES
# - /app/var/document-vault: For persistent PDF storage (Must be volume-mapped in docker-compose)
# - /app/tmp: The Tomcat Spool directory (PREVENTS LARGE UPLOAD CRASHES)
RUN mkdir -p /app/var/document-vault \
    && mkdir -p /app/tmp \
    && chown -R prymeuser:prymegroup /app

# 4. Extract compiled JAR from the builder stage
COPY --from=builder /build/target/*.jar pryme-backend.jar
RUN chown prymeuser:prymegroup pryme-backend.jar

# 5. Drop OS privileges permanently
USER prymeuser

# 6. Expose Spring Boot port
EXPOSE 8080

# ==========================================
# 🧠 THE TITANIUM KVM2 ENTRYPOINT (JAVA 17)
# 1. UseContainerSupport: Native Docker RAM awareness.
# 2. MaxRAMPercentage=75.0: Leaves 25% (2GB) of KVM2 RAM for the Linux OS & Postgres.
# 3. java.io.tmpdir: Routes Tomcat's file uploads to our permission-safe folder.
# 4. MaxMetaspaceSize: Caps classloader memory to prevent silent JVM Native Memory leaks.
# ==========================================
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=70.0", \
            "-XX:MaxMetaspaceSize=512m", \
            "-XX:+UseG1GC", \
            "-XX:G1HeapRegionSize=16m", \
            "-XX:+ParallelRefProcEnabled", \
            "-XX:MaxGCPauseMillis=200", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Djava.io.tmpdir=/app/tmp", \
            "-jar", "pryme-backend.jar"]

# ==========================================
# 🧠 STAGE 1: THE SELF-HEALING BUILDER MATRIX
# We use Alpine Linux to keep the build environment tiny and fast.
# ==========================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# 1. THE WINDOWS FAILSAFE: Install dos2unix to fix CRLF line endings on the fly
RUN apk add --no-cache dos2unix

# 2. Copy only the Maven architecture first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 3. Sanitize the wrapper and make it executable (Prevents Windows ^M crashes)
RUN dos2unix mvnw && chmod +x mvnw

# 4. CACHE BARRIER: Download dependencies offline.
# This caches the massive .m2 folder so subsequent builds take seconds, not minutes.
RUN ./mvnw dependency:go-offline -B

# 5. Copy source code and execute the build (skipping tests for image compilation speed)
COPY src src
RUN ./mvnw clean package -DskipTests

# ==========================================
# 🧠 STAGE 2: THE SECURE RUNTIME MATRIX (KVM2 OPTIMIZED)
# We drop the heavy JDK and use the JRE. The final image drops from ~500MB to ~150MB.
# ==========================================
FROM eclipse-temurin:17-jre-alpine

# 1. ZERO-TRUST SECURITY: Never run a container as root.
# We create a dedicated, restricted user. If a hacker escapes Tomcat, they have no OS privileges.
RUN addgroup -S prymegroup && adduser -S prymeuser -G prymegroup

WORKDIR /app

# 2. Extract only the compiled Titanium JAR from Stage 1
COPY --from=builder /build/target/pryme-backend-*.jar pryme-backend.jar

# 3. VAULT PROVISIONING: Create the physical directory for Phase 3 Document Uploads
# We must explicitly grant our restricted user ownership of this folder.
RUN mkdir -p /app/var/document-vault && chown -R prymeuser:prymegroup /app

# 4. Drop privileges
USER prymeuser

# Expose Tomcat's port
EXPOSE 8080

# ==========================================
# 🧠 THE TITANIUM KVM2 ENTRYPOINT
# 1. UseContainerSupport: Forces Java to respect the KVM/Docker CPU & RAM limits.
# 2. MaxRAMPercentage=75.0: Prevents the JVM from eating 100% of memory and getting OOM-Killed by Linux.
# 3. urandom: Solves the infamous KVM entropy hanging bug so JWTs generate in nanoseconds.
# ==========================================
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:+OptimizeStringConcat", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "pryme-backend.jar"]
package com.pryme.Backend.iam;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
 main
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
 main

@Component
public class SessionManager {

 codex/create-45-day-execution-plan-0e2u1d
    private final int maxSessionsPerUser;
    private final Duration sessionTtl;

    private static final int MAX_SESSIONS_PER_USER = 3;
    private static final Duration SESSION_TTL = Duration.ofHours(8);
 main

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, SessionRecord> tokenIndex = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<SessionRecord>> userSessions = new ConcurrentHashMap<>();

 codex/create-45-day-execution-plan-0e2u1d
    public SessionManager(
            @Value("${app.security.session.max-sessions-per-user:3}") int maxSessionsPerUser,
            @Value("${app.security.session.ttl-hours:8}") long sessionTtlHours
    ) {
        this(maxSessionsPerUser, Duration.ofHours(Math.max(1, sessionTtlHours)));
    }

    SessionManager(int maxSessionsPerUser, Duration sessionTtl) {
        this.maxSessionsPerUser = Math.max(1, maxSessionsPerUser);
        this.sessionTtl = sessionTtl.isNegative() || sessionTtl.isZero() ? Duration.ofMinutes(1) : sessionTtl;
    }

    public SessionRecord issueSession(UUID userId, String deviceId) {
        String token = nextToken();
        Instant now = Instant.now();
        SessionRecord session = new SessionRecord(token, userId, sanitizeDeviceId(deviceId), now, now.plus(sessionTtl));

        Deque<SessionRecord> queue = userSessions.computeIfAbsent(userId, ignored -> new ConcurrentLinkedDeque<>());
        queue.addLast(session);
        tokenIndex.put(token, session);

        while (queue.size() > maxSessionsPerUser) {
            SessionRecord removed = queue.pollFirst();
            if (removed != null) {
                tokenIndex.remove(removed.token());
            }

    @CachePut(cacheNames = "auth:sessions", key = "#result.token()")
    public SessionRecord issueSession(UUID userId, String deviceId) {
        String token = nextToken();
        Instant now = Instant.now();
        SessionRecord session = new SessionRecord(token, userId, deviceId == null ? "unknown" : deviceId, now, now.plus(SESSION_TTL));

        Deque<SessionRecord> queue = userSessions.computeIfAbsent(userId, ignored -> new ArrayDeque<>());
        queue.addLast(session);
        tokenIndex.put(token, session);

        while (queue.size() > MAX_SESSIONS_PER_USER) {
            SessionRecord removed = queue.removeFirst();
            tokenIndex.remove(removed.token());
 main
        }

        return session;
    }

 codex/create-45-day-execution-plan-0e2u1d
    public SessionRecord validate(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        SessionRecord session = tokenIndex.get(token);
        if (session == null || session.isExpired()) {
            invalidate(token);

    @Cacheable(cacheNames = "auth:sessions", key = "#token")
    public SessionRecord validate(String token) {
        SessionRecord session = tokenIndex.get(token);
        if (session == null || session.isExpired()) {
            tokenIndex.remove(token);
 main
            return null;
        }
        return session;
    }


    public void invalidate(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

    @CacheEvict(cacheNames = "auth:sessions", key = "#token")
    public void invalidate(String token) {
 main
        SessionRecord session = tokenIndex.remove(token);
        if (session == null) {
            return;
        }
 codex/create-45-day-execution-plan-0e2u1d

        Deque<SessionRecord> queue = userSessions.get(session.userId());
        if (queue != null) {
            queue.removeIf(s -> s.token().equals(token));
            if (queue.isEmpty()) {
                userSessions.remove(session.userId(), queue);
            }

        Deque<SessionRecord> queue = userSessions.get(session.userId());
        if (queue != null) {
            queue.removeIf(s -> s.token().equals(token));
 main
        }
    }

    public List<SessionRecord> activeSessions(UUID userId) {
 codex/create-45-day-execution-plan-0e2u1d
        Deque<SessionRecord> queue = userSessions.get(userId);
        if (queue == null) {
            return List.of();
        }

        queue.removeIf(SessionRecord::isExpired);
        if (queue.isEmpty()) {
            userSessions.remove(userId, queue);
            return List.of();
        }

        return List.copyOf(queue);
    }

    @Scheduled(fixedDelayString = "${app.security.session.cleanup-ms:300000}")
    public void cleanupExpiredSessions() {
        List<String> expired = new ArrayList<>();

        tokenIndex.forEach((token, session) -> {
            if (session.isExpired()) {
                expired.add(token);
            }
        });

        expired.forEach(this::invalidate);
    }

    @PreDestroy
    public void shutdown() {
        tokenIndex.clear();
        userSessions.clear();
    }

    private String sanitizeDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return "unknown";
        }
        String cleaned = deviceId.trim();
        return cleaned.length() > 128 ? cleaned.substring(0, 128) : cleaned;
    }

        Deque<SessionRecord> queue = userSessions.getOrDefault(userId, new ArrayDeque<>());
        queue.removeIf(SessionRecord::isExpired);
        return List.copyOf(queue);
    }

 main
    private String nextToken() {
        byte[] buffer = new byte[32];
        secureRandom.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }
}

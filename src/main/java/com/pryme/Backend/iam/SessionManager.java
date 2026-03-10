package com.pryme.Backend.iam;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private static final int MAX_SESSIONS_PER_USER = 3;
    private static final Duration SESSION_TTL = Duration.ofHours(8);

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, SessionRecord> tokenIndex = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<SessionRecord>> userSessions = new ConcurrentHashMap<>();

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
        }

        return session;
    }

    @Cacheable(cacheNames = "auth:sessions", key = "#token")
    public SessionRecord validate(String token) {
        SessionRecord session = tokenIndex.get(token);
        if (session == null || session.isExpired()) {
            tokenIndex.remove(token);
            return null;
        }
        return session;
    }

    @CacheEvict(cacheNames = "auth:sessions", key = "#token")
    public void invalidate(String token) {
        SessionRecord session = tokenIndex.remove(token);
        if (session == null) {
            return;
        }
        Deque<SessionRecord> queue = userSessions.get(session.userId());
        if (queue != null) {
            queue.removeIf(s -> s.token().equals(token));
        }
    }

    public List<SessionRecord> activeSessions(UUID userId) {
        Deque<SessionRecord> queue = userSessions.getOrDefault(userId, new ArrayDeque<>());
        queue.removeIf(SessionRecord::isExpired);
        return List.copyOf(queue);
    }

    private String nextToken() {
        byte[] buffer = new byte[32];
        secureRandom.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }
}

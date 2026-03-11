package com.pryme.Backend.iam;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class SessionManager {

    private final int maxSessionsPerUser;
    private final Duration sessionTtl;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, SessionRecord> tokenIndex = new ConcurrentHashMap<String, SessionRecord>();
    private final Map<UUID, Deque<SessionRecord>> userSessions = new ConcurrentHashMap<UUID, Deque<SessionRecord>>();

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

        Deque<SessionRecord> queue = userSessions.get(userId);
        if (queue == null) {
            Deque<SessionRecord> newQueue = new ConcurrentLinkedDeque<SessionRecord>();
            Deque<SessionRecord> existing = userSessions.putIfAbsent(userId, newQueue);
            queue = existing != null ? existing : newQueue;
        }

        queue.addLast(session);
        tokenIndex.put(token, session);

        while (queue.size() > maxSessionsPerUser) {
            SessionRecord removed = queue.pollFirst();
            if (removed != null) {
                tokenIndex.remove(removed.token());
            }
        }

        return session;
    }

    public SessionRecord validate(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        SessionRecord session = tokenIndex.get(token);
        if (session == null || session.isExpired()) {
            invalidate(token);
            return null;
        }
        return session;
    }

    public void invalidate(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        SessionRecord session = tokenIndex.remove(token);
        if (session == null) {
            return;
        }

        Deque<SessionRecord> queue = userSessions.get(session.userId());
        if (queue != null) {
            List<SessionRecord> snapshot = new ArrayList<SessionRecord>(queue);
            for (SessionRecord record : snapshot) {
                if (token.equals(record.token())) {
                    queue.remove(record);
                }
            }
            if (queue.isEmpty()) {
                userSessions.remove(session.userId(), queue);
            }
        }
    }

    public List<SessionRecord> activeSessions(UUID userId) {
        Deque<SessionRecord> queue = userSessions.get(userId);
        if (queue == null) {
            return Collections.emptyList();
        }

        List<SessionRecord> snapshot = new ArrayList<SessionRecord>(queue);
        for (SessionRecord session : snapshot) {
            if (session.isExpired()) {
                queue.remove(session);
            }
        }

        if (queue.isEmpty()) {
            userSessions.remove(userId, queue);
            return Collections.emptyList();
        }

        return new ArrayList<SessionRecord>(queue);
    }

    @Scheduled(fixedDelayString = "${app.security.session.cleanup-ms:300000}")
    public void cleanupExpiredSessions() {
        List<String> expired = new ArrayList<String>();

        for (Map.Entry<String, SessionRecord> entry : tokenIndex.entrySet()) {
            if (entry.getValue().isExpired()) {
                expired.add(entry.getKey());
            }
        }

        for (String token : expired) {
            invalidate(token);
        }
    }

    @PreDestroy
    public void shutdown() {
        tokenIndex.clear();
        userSessions.clear();
    }

    private String sanitizeDeviceId(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return "unknown";
        }
        String cleaned = deviceId.trim();
        return cleaned.length() > 128 ? cleaned.substring(0, 128) : cleaned;
    }

    private String nextToken() {
        byte[] buffer = new byte[32];
        secureRandom.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }
}

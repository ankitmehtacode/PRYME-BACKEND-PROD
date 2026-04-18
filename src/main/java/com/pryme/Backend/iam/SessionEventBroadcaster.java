package com.pryme.Backend.iam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 SSE SESSION EVENT BROADCASTER — THE KILL SWITCH PIPE
 *
 * Thread-safe registry of Server-Sent Event emitters keyed by userId and
 * sessionId.
 */
@Component
public class SessionEventBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(SessionEventBroadcaster.class);

    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    // PRIMARY REGISTRY: sessionId → SseEmitter
    private final Map<UUID, SseEmitter> sessionEmitterRegistry = new ConcurrentHashMap<>();

    // SECONDARY REGISTRY: userId → Set<sessionId>
    private final Map<UUID, java.util.Set<UUID>> userSessionMap = new ConcurrentHashMap<>();

    public SseEmitter register(UUID userId, UUID sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        sessionEmitterRegistry.put(sessionId, emitter);
        userSessionMap.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        Runnable cleanup = () -> {
            sessionEmitterRegistry.remove(sessionId);
            java.util.Set<UUID> sessions = userSessionMap.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessionMap.remove(userId);
                }
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        log.debug("SSE Kill Switch: Emitter registered via Session {}, active user streams: {}",
                sessionId, userSessionMap.getOrDefault(userId, java.util.Collections.emptySet()).size());

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("{\"status\": \"SSE_PIPE_ACTIVE\"}"));
        } catch (IOException e) {
            log.warn("SSE Kill Switch: Failed to send initial handshake to Session {}", sessionId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 🧠 PUSH TARGETED SESSION_TERMINATED EVENT
     */
    public void pushSessionTermination(List<UUID> sessionIdsToKill) {
        if (sessionIdsToKill == null || sessionIdsToKill.isEmpty())
            return;

        log.info("SSE Kill Switch: Surgically pushing SESSION_TERMINATED to {} specific sessions.",
                sessionIdsToKill.size());

        for (UUID sessionId : sessionIdsToKill) {
            SseEmitter emitter = sessionEmitterRegistry.get(sessionId);
            if (emitter != null) {
                try {
                    // 🧠 THE ANTIGRAVITY BRIDGE: Send the exact ID in a JSON array format
                    // so the upgraded useAuth.ts parser catches it perfectly.
                    String targetedPayload = "[\"" + sessionId.toString() + "\"]";

                    emitter.send(SseEmitter.event()
                            .name("SESSION_TERMINATED")
                            .data(targetedPayload));

                    emitter.complete();
                } catch (IOException e) {
                    log.warn("SSE Kill Switch: Failed to push termination to stale Session {}", sessionId);
                    emitter.completeWithError(e);
                }
                sessionEmitterRegistry.remove(sessionId);
            }
        }
    }

    /**
     * 🧠 PUSH GLOBAL SESSION_TERMINATED EVENT
     */
    public void pushTermination(UUID userId) {
        java.util.Set<UUID> sessions = userSessionMap.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        // Copy list to avoid concurrent modification issues
        List<UUID> snapshotSessionsToKill = new java.util.ArrayList<>(sessions);

        // This will route through the surgical strike above, sending each tab its
        // specific ID to die.
        pushSessionTermination(snapshotSessionsToKill);
        userSessionMap.remove(userId);
    }

    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        List<UUID> deadSessions = new java.util.ArrayList<>();

        sessionEmitterRegistry.forEach((sessionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("HEARTBEAT")
                        .data("{\"ts\": " + System.currentTimeMillis() + "}"));
            } catch (IOException e) {
                deadSessions.add(sessionId);
            }
        });

        if (!deadSessions.isEmpty()) {
            deadSessions.forEach(sessionId -> {
                sessionEmitterRegistry.remove(sessionId);
                userSessionMap.forEach((uId, set) -> set.remove(sessionId));
            });
            log.debug("SSE Kill Switch: Purged {} dead emitters.", deadSessions.size());
        }
    }

    public int getActiveConnectionCount() {
        return sessionEmitterRegistry.size();
    }
}
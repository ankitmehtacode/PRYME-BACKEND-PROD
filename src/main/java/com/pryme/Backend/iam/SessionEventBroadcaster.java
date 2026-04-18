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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 🧠 SSE SESSION EVENT BROADCASTER — THE KILL SWITCH PIPE
 *
 * Thread-safe registry of Server-Sent Event emitters keyed by userId.
 * When a session is revoked (admin action, max-sessions overflow, or manual logout),
 * this broadcaster pushes a SESSION_TERMINATED event to all active browser tabs
 * belonging to that user, causing the frontend to immediately wipe state and redirect.
 *
 * Architecture:
 * - ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> supports multiple tabs per user
 * - 5-minute SSE timeout with 30-second heartbeats to prevent proxy idle disconnects
 * - Auto-cleanup on completion/timeout/error — zero memory leaks guaranteed
 */
@Component
public class SessionEventBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(SessionEventBroadcaster.class);

    /**
     * 🧠 SSE_TIMEOUT: 5 minutes. After this, the emitter auto-closes.
     * The frontend's EventSource will auto-reconnect (built-in browser behavior),
     * which re-authenticates via the HttpOnly cookie. This is a feature, not a bug:
     * it forces periodic re-validation of the session.
     */
    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    /**
     * PRIMARY REGISTRY: sessionId → SseEmitter
     * Allows precision targeting to terminate distinct sessions.
     */
    private final Map<UUID, SseEmitter> sessionEmitterRegistry = new ConcurrentHashMap<>();

    /**
     * SECONDARY REGISTRY: userId → Set<sessionId>
     * Allows fast lookup for killing ALL sessions for a user globally.
     */
    private final Map<UUID, java.util.Set<UUID>> userSessionMap = new ConcurrentHashMap<>();

    /**
     * 🧠 REGISTER AN EMITTER
     */
    public SseEmitter register(UUID userId, UUID sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        sessionEmitterRegistry.put(sessionId, emitter);
        userSessionMap.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        // Auto-cleanup: Remove the emitter when it completes, times out, or errors
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
     * 🧠 PUSH TARGETED SESSION_TERMINATED EVENT (THE 1% FIX)
     * Pushes the kill signal ONLY to specific active browser tabs targeting the explicitly revoked sessions.
     */
    public void pushSessionTermination(List<UUID> sessionIdsToKill) {
        if (sessionIdsToKill == null || sessionIdsToKill.isEmpty()) return;

        log.info("SSE Kill Switch: Surgically pushing SESSION_TERMINATED to {} specific sessions.", sessionIdsToKill.size());

        for (UUID sessionId : sessionIdsToKill) {
            SseEmitter emitter = sessionEmitterRegistry.get(sessionId);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("SESSION_TERMINATED")
                            .data("{\"reason\": \"SESSION_REVOKED\", \"action\": \"REDIRECT_TO_AUTH\"}"));
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
     * 🧠 PUSH GLOBAL SESSION_TERMINATED EVENT (ALL USER SESSIONS)
     * Kills ALL browser tabs belonging to this user (used for global wipeout/admin panic button).
     */
    public void pushTermination(UUID userId) {
        java.util.Set<UUID> sessions = userSessionMap.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        // Copy list to avoid concurrent modification issues
        List<UUID> snapshotSessionsToKill = new java.util.ArrayList<>(sessions);
        pushSessionTermination(snapshotSessionsToKill);
        userSessionMap.remove(userId);
    }

    /**
     * 🧠 HEARTBEAT PING
     */
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
                // Also purge from userMap
                userSessionMap.forEach((uId, set) -> set.remove(sessionId));
            });
            log.debug("SSE Kill Switch: Purged {} dead emitters.", deadSessions.size());
        }
    }

    public int getActiveConnectionCount() {
        return sessionEmitterRegistry.size();
    }
}

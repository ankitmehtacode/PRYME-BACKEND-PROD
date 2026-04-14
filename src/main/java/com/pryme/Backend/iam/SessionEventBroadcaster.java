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
     * Registry: userId → list of active SSE connections (one per browser tab)
     */
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitterRegistry = new ConcurrentHashMap<>();

    /**
     * 🧠 REGISTER AN EMITTER
     * Called by SystemEventController when a client connects to /api/v1/stream/system-events.
     * Returns the configured SseEmitter with auto-cleanup callbacks.
     */
    public SseEmitter register(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitterRegistry.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // Auto-cleanup: Remove the emitter when it completes, times out, or errors
        Runnable cleanup = () -> {
            CopyOnWriteArrayList<SseEmitter> emitters = emitterRegistry.get(userId);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    emitterRegistry.remove(userId);
                }
            }
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        log.debug("SSE Kill Switch: Emitter registered for User {}, total active: {}",
                userId, emitterRegistry.getOrDefault(userId, new CopyOnWriteArrayList<>()).size());

        // 🧠 Send an initial connection confirmation event
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("{\"status\": \"SSE_PIPE_ACTIVE\"}"));
        } catch (IOException e) {
            log.warn("SSE Kill Switch: Failed to send initial handshake to User {}", userId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 🧠 PUSH SESSION_TERMINATED EVENT
     * Called by SessionManager when revokeSession() or revokeAllUserSessions() fires.
     * Pushes the kill signal to ALL browser tabs belonging to this user.
     */
    public void pushTermination(UUID userId) {
        CopyOnWriteArrayList<SseEmitter> emitters = emitterRegistry.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("SSE Kill Switch: No active emitters for User {}. They will hit 401 on next API call.", userId);
            return;
        }

        log.info("SSE Kill Switch: Pushing SESSION_TERMINATED to {} active connections for User {}",
                emitters.size(), userId);

        // 🧠 Iterate a snapshot — CopyOnWriteArrayList is safe for concurrent modification
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("SESSION_TERMINATED")
                        .data("{\"reason\": \"SESSION_REVOKED\", \"action\": \"REDIRECT_TO_AUTH\"}"));
                emitter.complete(); // Close the connection after sending the kill signal
            } catch (IOException e) {
                log.warn("SSE Kill Switch: Failed to push termination to a stale emitter for User {}", userId);
                emitter.completeWithError(e);
            }
        }

        // Nuke all emitters for this user after termination
        emitterRegistry.remove(userId);
    }

    /**
     * 🧠 HEARTBEAT PING — Keeps SSE connections alive through Nginx/ALB/CloudFront proxies.
     * Without this, reverse proxies silently kill idle TCP connections after 60 seconds,
     * causing the EventSource to reconnect in a storm.
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void heartbeat() {
        emitterRegistry.forEach((userId, emitters) -> {
            List<SseEmitter> deadEmitters = new java.util.ArrayList<>();

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("HEARTBEAT")
                            .data("{\"ts\": " + System.currentTimeMillis() + "}"));
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }

            // Purge dead emitters
            if (!deadEmitters.isEmpty()) {
                emitters.removeAll(deadEmitters);
                if (emitters.isEmpty()) {
                    emitterRegistry.remove(userId);
                }
                log.debug("SSE Kill Switch: Purged {} dead emitters for User {}", deadEmitters.size(), userId);
            }
        });
    }

    /**
     * Returns the count of active SSE connections (for monitoring/actuator).
     */
    public int getActiveConnectionCount() {
        return emitterRegistry.values().stream().mapToInt(List::size).sum();
    }
}

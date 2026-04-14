package com.pryme.Backend.iam;

import com.pryme.Backend.common.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * 🧠 SSE SYSTEM EVENT STREAM — THE KILL SWITCH ENDPOINT
 *
 * Exposes GET /api/v1/stream/system-events as an SSE (Server-Sent Events) stream.
 * The frontend's EventSource connects here on app boot with withCredentials: true.
 *
 * When a session is revoked by an admin or exceeds max-devices, the backend
 * pushes a SESSION_TERMINATED event through this pipe, causing the frontend
 * to immediately wipe its React Query cache and redirect to /auth.
 *
 * Security:
 * - AUTHENTICATED: Requires a valid PRYME_SID HttpOnly cookie
 * - NOT public: Falls under anyRequest().authenticated() in SecurityConfig
 * - The cookie's SameSite=Strict prevents CSRF abuse of this endpoint
 */
@RestController
@RequestMapping("/api/v1/stream")
public class SystemEventController {

    private final SessionEventBroadcaster broadcaster;

    public SystemEventController(SessionEventBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    /**
     * 🧠 SSE CONNECTION ENDPOINT
     *
     * The browser opens a persistent HTTP connection here.
     * Spring holds the SseEmitter open until timeout or explicit completion.
     * The frontend receives events as they're pushed by SessionManager.
     *
     * EventSource auto-reconnects on disconnect (built-in browser behavior),
     * which re-validates the session cookie on each reconnection.
     */
    @Operation(summary = "SSE stream for real-time session and system events")
    @GetMapping(value = "/system-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSystemEvents(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            throw new UnauthorizedException("Valid session required for event stream.");
        }

        return broadcaster.register(userId);
    }
}

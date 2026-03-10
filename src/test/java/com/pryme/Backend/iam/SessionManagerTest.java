package com.pryme.Backend.iam;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    @Test
    void issueSessionEnforcesMaxSessionsPerUser() {
        SessionManager manager = new SessionManager(2, Duration.ofHours(8));
        UUID userId = UUID.randomUUID();

        SessionRecord first = manager.issueSession(userId, "d1");
        SessionRecord second = manager.issueSession(userId, "d2");
        SessionRecord third = manager.issueSession(userId, "d3");

        List<SessionRecord> active = manager.activeSessions(userId);
        assertEquals(2, active.size());
        assertFalse(active.stream().anyMatch(s -> s.token().equals(first.token())));
        assertNotNull(manager.validate(second.token()));
        assertNotNull(manager.validate(third.token()));
        assertNull(manager.validate(first.token()));
    }

    @Test
    void issueSessionSanitizesAndTruncatesDeviceId() {
        SessionManager manager = new SessionManager(3, Duration.ofHours(8));
        UUID userId = UUID.randomUUID();

        String longDeviceId = "x".repeat(256);
        SessionRecord session = manager.issueSession(userId, longDeviceId);

        assertEquals(128, session.deviceId().length());
    }

    @Test
    void cleanupRemovesExpiredSessions() throws InterruptedException {
        SessionManager manager = new SessionManager(3, Duration.ofMillis(2));
        UUID userId = UUID.randomUUID();
        SessionRecord session = manager.issueSession(userId, "web");

        Thread.sleep(10);
        manager.cleanupExpiredSessions();

        assertNull(manager.validate(session.token()));
        assertTrue(manager.activeSessions(userId).isEmpty());
    }

    @Test
    void validateRejectsBlankToken() {
        SessionManager manager = new SessionManager(3, Duration.ofHours(8));
        assertNull(manager.validate(null));
        assertNull(manager.validate(" "));
    }
}

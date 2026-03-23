package com.pryme.Backend.iam;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    @Test
    void issueSessionEnforcesMaxSessionsPerUser() {
        SessionManager manager = new SessionManager();
        UUID userId = UUID.randomUUID();

        User user1 = new User();
        user1.setId(userId);

        SessionRecord first = manager.registerSession(UUID.randomUUID(), user1, Instant.now().plusSeconds(3600), "127.0.0.1", "TestAgent");
        SessionRecord second = manager.registerSession(UUID.randomUUID(), user1, Instant.now().plusSeconds(3600), "127.0.0.1", "TestAgent");
        SessionRecord third = manager.registerSession(UUID.randomUUID(), user1, Instant.now().plusSeconds(3600), "127.0.0.1", "TestAgent");

        List<SessionRecord> active = manager.activeSessions(userId);
        assertEquals(2, active.size());
        assertFalse(active.stream().anyMatch(s -> s.getId().toString().equals(first.getId().toString())));
        assertNotNull(manager.validate(second.getId().toString()));
        assertNotNull(manager.validate(third.getId().toString()));
        assertNull(manager.validate(first.getId().toString()));
    }

    @Test
    void issueSessionSanitizesAndTruncatesDeviceId() {
        SessionManager manager = new SessionManager();
        UUID userId = UUID.randomUUID();

        User user2 = new User();
        user2.setId(userId);

        String longDeviceId = "x".repeat(256);
        SessionRecord session = manager.registerSession(UUID.randomUUID(), user2, Instant.now().plusSeconds(3600), "127.0.0.1", "TestAgent");

        assertEquals(128, session.getUserAgent().length());
    }

    @Test
    void cleanupRemovesExpiredSessions() throws InterruptedException {
        SessionManager manager = new SessionManager();
        UUID userId = UUID.randomUUID();

        User user3 = new User();
        user3.setId(userId);

        SessionRecord session = manager.registerSession(UUID.randomUUID(), user3, Instant.now().plusSeconds(2), "127.0.0.1", "TestAgent");

        Thread.sleep(10);
        manager.sweepDeadSessions();

        assertNull(manager.validate(session.getId().toString()));
        assertTrue(manager.activeSessions(userId).isEmpty());
    }

    @Test
    void validateRejectsBlankToken() {
        SessionManager manager = new SessionManager();
        assertNull(manager.validate(null));
        assertNull(manager.validate(" "));
    }
}

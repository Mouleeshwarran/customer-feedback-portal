
package com.feedback.portal.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Simple in-memory login throttle.
 * Allows up to MAX_FAILURES failed attempts per WINDOW_MILLIS.
 * The counter resets automatically after the window expires.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MILLIS = 15 * 60_000L; // 15 minutes

    private final ConcurrentMap<String, Deque<Long>> failures = new ConcurrentHashMap<>();

    public void recordFailure(String identifier) {
        String key = identifier.trim().toLowerCase();
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = failures.computeIfAbsent(
            key, ignored -> new ArrayDeque<>()
        );

        synchronized (timestamps) {
            evictOld(timestamps, now);
            timestamps.addLast(now);

            if (timestamps.size() >= MAX_FAILURES) {
                throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed login attempts. Please try again later."
                );
            }
        }
    }

    public void recordSuccess(String identifier) {
        failures.remove(identifier.trim().toLowerCase());
    }

    private void evictOld(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty()
                && now - timestamps.peekFirst() >= WINDOW_MILLIS) {
            timestamps.removeFirst();
        }
    }
}

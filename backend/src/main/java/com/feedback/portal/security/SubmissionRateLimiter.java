
package com.feedback.portal.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SubmissionRateLimiter {

    private static final int MAX_SUBMISSIONS = 3;
    private static final long WINDOW_MILLIS = 60_000L;

    private final ConcurrentMap<String, Deque<Long>> attempts =
            new ConcurrentHashMap<>();

    public void checkLimit(String email) {

        String key = email.trim().toLowerCase();
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = attempts.computeIfAbsent(
            key, ignored -> new ArrayDeque<>()
        );

        synchronized (timestamps) {

            while (!timestamps.isEmpty()
                    && now - timestamps.peekFirst() >= WINDOW_MILLIS) {
                timestamps.removeFirst();
            }

            if (timestamps.size() >= MAX_SUBMISSIONS) {
                throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Submission limit reached. Please try again later."
                );
            }

            timestamps.addLast(now);
        }
    }
}

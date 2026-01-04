package com.billboarding.configs;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration using Bucket4j.
 * Provides per-IP rate limiting for different API endpoints.
 */
@Component
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Auth APIs: 10 requests per minute per IP
     * Protects against brute-force login attempts.
     */
    public Bucket resolveAuthBucket(String key) {
        return buckets.computeIfAbsent("auth:" + key, k -> createAuthBucket());
    }

    /**
     * Booking APIs: 30 requests per minute per IP
     * Prevents booking spam while allowing normal usage.
     */
    public Bucket resolveBookingBucket(String key) {
        return buckets.computeIfAbsent("booking:" + key, k -> createBookingBucket());
    }

    /**
     * General APIs: 100 requests per minute per IP
     */
    public Bucket resolveGeneralBucket(String key) {
        return buckets.computeIfAbsent("general:" + key, k -> createGeneralBucket());
    }

    private Bucket createAuthBucket() {
        // New API: Bandwidth.simple(tokens, duration)
        Bandwidth limit = Bandwidth.simple(10, Duration.ofMinutes(1));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createBookingBucket() {
        Bandwidth limit = Bandwidth.simple(30, Duration.ofMinutes(1));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createGeneralBucket() {
        Bandwidth limit = Bandwidth.simple(100, Duration.ofMinutes(1));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Clear all buckets (for testing or reset purposes)
     */
    public void clearBuckets() {
        buckets.clear();
    }
}

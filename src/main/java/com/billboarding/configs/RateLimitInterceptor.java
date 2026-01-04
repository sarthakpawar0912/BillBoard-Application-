package com.billboarding.configs;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rate Limit Interceptor.
 * Applies per-IP rate limiting based on API endpoint type.
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        Bucket bucket;

        // Select appropriate bucket based on endpoint
        if (isAuthEndpoint(path)) {
            bucket = rateLimitConfig.resolveAuthBucket(clientIp);
        } else if (isBookingEndpoint(path)) {
            bucket = rateLimitConfig.resolveBookingBucket(clientIp);
        } else {
            bucket = rateLimitConfig.resolveGeneralBucket(clientIp);
        }

        if (bucket.tryConsume(1)) {
            return true; // Request allowed
        }

        // Rate limit exceeded
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            "{\"success\": false, \"message\": \"Too many requests. Please try again later.\", \"status\": 429}"
        );
        return false;
    }

    private boolean isAuthEndpoint(String path) {
        return path.contains("/api/auth/") ||
               path.contains("/api/login") ||
               path.contains("/api/register") ||
               path.contains("/api/forgot-password") ||
               path.contains("/api/verify-otp");
    }

    private boolean isBookingEndpoint(String path) {
        return path.contains("/api/bookings") ||
               path.contains("/api/booking/");
    }

    /**
     * Extract client IP, considering proxy headers.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

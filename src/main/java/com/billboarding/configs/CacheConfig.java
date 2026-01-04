package com.billboarding.configs;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache Configuration for the application.
 * Uses in-memory ConcurrentMapCache for simplicity.
 * For production with multiple instances, consider Redis or Hazelcast.
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "platformSettings",  // Platform configuration (rarely changes)
            "userRoles"          // User role lookups
        );
    }
}

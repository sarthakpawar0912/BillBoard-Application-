package com.billboarding.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson Configuration for handling Hibernate lazy-loaded proxies.
 * Prevents serialization errors when returning entities with LAZY relationships.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // Register Hibernate6Module to handle lazy-loaded proxies
        Hibernate6Module hibernate6Module = new Hibernate6Module();

        // Configure to not force lazy loading during serialization
        hibernate6Module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);

        // Serialize identifier for lazy not loaded objects
        hibernate6Module.configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);

        objectMapper.registerModule(hibernate6Module);

        // Don't fail on empty beans (lazy proxies)
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return objectMapper;
    }
}

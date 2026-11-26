package com.example.product_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * L1: Caffeine (in-memory, TTL = 5s)
 * L2: Redis (distributed, TTL = 60s)
 */
@Configuration
@EnableCaching
public class CacheConfig {

        public static final String L1_CACHE_MANAGER = "caffeineCacheManager";
        public static final String L2_CACHE_MANAGER = "redisCacheManager";

        public static final String CACHE_PRODUCTS = "products";
        public static final String CACHE_PRODUCT = "product";

        // Caffeine
        @Bean(L1_CACHE_MANAGER)
        @Primary
        public CacheManager caffeineCacheManager() {
                CaffeineCacheManager manager = new CaffeineCacheManager(CACHE_PRODUCTS, CACHE_PRODUCT);
                manager.setCaffeine(
                                Caffeine.newBuilder()
                                                .expireAfterWrite(5, TimeUnit.SECONDS)
                                                .maximumSize(500)
                                                .recordStats());
                return manager;
        }

        // Redis
        @Bean(L2_CACHE_MANAGER)
        public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(60))
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(
                                                                new GenericJackson2JsonRedisSerializer()))
                                .disableCachingNullValues();

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(config)
                                .build();
        }
}

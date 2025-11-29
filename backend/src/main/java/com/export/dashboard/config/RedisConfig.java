package com.export.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // JSON 직렬화 설정
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        // Key serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serializer
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // 기본 TTL 10분
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 캐시별 개별 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 대시보드 요약 데이터는 30분 캐시
        cacheConfigurations.put("dashboard-summary",
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(30)));

        // 국가/제품 마스터 데이터는 1시간 캐시
        cacheConfigurations.put("countries",
                defaultCacheConfiguration.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("product-categories",
                defaultCacheConfiguration.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("main-categories",
                defaultCacheConfiguration.entryTtl(Duration.ofHours(1)));

        // 지역/대륙 데이터는 1일 캐시
        cacheConfigurations.put("regions",
                defaultCacheConfiguration.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("continents",
                defaultCacheConfiguration.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("hs-levels",
                defaultCacheConfiguration.entryTtl(Duration.ofDays(1)));

        // 수출 통계는 15분 캐시
        cacheConfigurations.put("export-statistics",
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("yearly-trend",
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)));

        // 지역/대륙별 필터링 데이터는 1시간 캐시
        cacheConfigurations.put("countries-by-region",
                defaultCacheConfiguration.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("countries-by-continent",
                defaultCacheConfiguration.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("product-categories-by-level",
                defaultCacheConfiguration.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("product-categories-by-parent",
                defaultCacheConfiguration.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
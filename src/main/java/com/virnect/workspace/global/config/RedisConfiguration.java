package com.virnect.workspace.global.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import com.virnect.workspace.dto.response.WorkspaceInfoListResponse;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-27
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@EnableCaching
@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {
	private final RedisConnectionFactory redisConnectionFactory;
	private final ObjectMapper objectMapper;

	public Jackson2JsonRedisSerializer<WorkspaceInfoListResponse> jackson2JsonRedisSerializer() {
		Jackson2JsonRedisSerializer<WorkspaceInfoListResponse> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
			WorkspaceInfoListResponse.class);
		jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
		return jackson2JsonRedisSerializer;
	}

	@Bean
	public RedisCacheManager redisCacheManager() {
		RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory);

		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
			.disableCachingNullValues()
			.entryTtl(Duration.ofSeconds(3600))
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer()));
		return builder.cacheDefaults(configuration).build();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer());
		return redisTemplate;
	}

}

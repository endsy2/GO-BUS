package com.busapp.bookingservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis configuration — Lettuce standalone with connection pooling (commons-pool2).
 *
 * Two templates are exposed:
 *  - StringRedisTemplate   → String/String, used for Bakong token caching
 *  - RedisTemplate<String, Object> → String key / JSON value, for any domain-object caching
 *
 * Pool sizing guidance (adjust per load):
 *   max-active  = number of concurrent Redis calls (rule of thumb: CPU cores × 2)
 *   min-idle    = keep-alive connections to avoid cold-start latency
 *   max-wait    = how long a caller blocks waiting for a free connection before failing
 */
@Configuration
public class RedisConfig {

    /* ── Connection ─────────────────────────────────────────────────────────── */

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.timeout:2000ms}")
    private Duration commandTimeout;

    /* ── Pool ───────────────────────────────────────────────────────────────── */

    @Value("${spring.data.redis.lettuce.pool.max-active:8}")
    private int poolMaxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
    private int poolMaxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:2}")
    private int poolMinIdle;

    @Value("${spring.data.redis.lettuce.pool.max-wait:-1ms}")
    private Duration poolMaxWait;

    /* ── Lettuce client resources (thread/event-loop pools) ─────────────────── */

    /**
     * Shared Lettuce I/O thread pools.  Must be destroyed on shutdown via
     * the destroyMethod — otherwise the JVM may hang on exit.
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources lettuceClientResources() {
        return DefaultClientResources.create();
    }

    /* ── Standalone connection configuration ────────────────────────────────── */

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }
        return config;
    }

    /* ── Connection factory ─────────────────────────────────────────────────── */

    @Primary
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            RedisStandaloneConfiguration standaloneConfig,
            ClientResources lettuceClientResources) {

        // Commons-pool2 pool config
        GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(poolMaxActive);
        poolConfig.setMaxIdle(poolMaxIdle);
        poolConfig.setMinIdle(poolMinIdle);
        poolConfig.setMaxWait(poolMaxWait);
        poolConfig.setTestOnBorrow(true);   // validate connection before handing it out
        poolConfig.setTestWhileIdle(true);  // evict broken idle connections

        // Low-level Lettuce client options
        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(SocketOptions.builder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build())
                .timeoutOptions(TimeoutOptions.enabled(commandTimeout))
                .autoReconnect(true)
                // Reject commands immediately when disconnected instead of queuing
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();

        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .clientOptions(clientOptions)
                .clientResources(lettuceClientResources)
                .commandTimeout(commandTimeout)
                .shutdownTimeout(Duration.ofSeconds(2))
                .poolConfig(poolConfig)
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standaloneConfig, clientConfig);
        // Required when using pooling — each borrow gets its own physical connection
        factory.setShareNativeConnection(false);
        return factory;
    }

    /* ── StringRedisTemplate ────────────────────────────────────────────────── */

    /**
     * String/String template.
     * Used for Bakong token caching.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }

    /* ── Generic Object Template ────────────────────────────────────────────── */

    /**
     * String key / JSON value template for caching domain objects.
     * Jackson serializer includes type metadata so objects can be
     * deserialized back to their original class without casting.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.setDefaultSerializer(jsonSerializer);
        template.setEnableTransactionSupport(false); // enable only if you use MULTI/EXEC
        template.afterPropertiesSet();
        return template;
    }

    /**
     * ObjectMapper for Redis JSON serialization.
     * - JavaTimeModule: handles Java 8+ date/time types (LocalDate, Instant, …)
     * - ActivateDefaultTyping: embeds "@class" so values can be deserialized
     *   back to their concrete type without requiring explicit type hints.
     */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return mapper;
    }
}

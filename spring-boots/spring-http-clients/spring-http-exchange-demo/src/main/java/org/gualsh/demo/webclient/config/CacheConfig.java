package org.gualsh.demo.webclient.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * Конфигурация кэширования с Caffeine.
 *
 * <p>Настраивает различные кэши с индивидуальными политиками:</p>
 * <ul>
 *   <li>users - кэш пользователей (долгосрочное хранение)</li>
 *   <li>posts - кэш постов (среднесрочное хранение)</li>
 *   <li>weather - кэш погодных данных (краткосрочное хранение)</li>
 * </ul>
 *
 * <p>Caffeine обеспечивает высокую производительность и эффективное
 * управление памятью с поддержкой различных политик выселения.</p>
 *
 * @see Caffeine
 * @see CacheManager
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Основной менеджер кэша с настройками по умолчанию.
     *
     * <p>Использует Caffeine с базовыми настройками производительности:</p>
     * <ul>
     *   <li>Максимум 1000 записей</li>
     *   <li>Время жизни 5 минут</li>
     *   <li>Статистика включена для мониторинга</li>
     *   <li>Асинхронный режим для reactive операций</li>
     * </ul>
     *
     * @return настроенный CacheManager
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        log.info("Configuring Caffeine Cache Manager with async support");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // ВАЖНО: Включаем асинхронный режим для reactive операций
        cacheManager.setAsyncCacheMode(true);

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats() // Включаем статистику для мониторинга
            .removalListener((key, value, cause) ->
                log.debug("Cache entry removed: key={}, cause={}", key, cause))
        );

        // Предустановленные имена кэшей
        cacheManager.setCacheNames(java.util.List.of("users", "posts", "weather"));

        return cacheManager;
    }

    /**
     * Специализированный кэш для пользователей.
     *
     * <p>Долгосрочное хранение пользовательских данных:</p>
     * <ul>
     *   <li>Максимум 500 пользователей</li>
     *   <li>Время жизни 30 минут</li>
     *   <li>Обновление при доступе</li>
     *   <li>Асинхронный режим для reactive операций</li>
     * </ul>
     *
     * @return кэш менеджер для пользователей
     */
    @Bean("usersCacheManager")
    @ConditionalOnProperty(name = "cache.users.enabled", havingValue = "true", matchIfMissing = false)
    public CacheManager usersCacheManager() {
        log.info("Configuring specialized Users Cache Manager with async support");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager("users");
        cacheManager.setAsyncCacheMode(true); // Включаем асинхронный режим
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .recordStats()
        );

        return cacheManager;
    }

    /**
     * Специализированный кэш для погодных данных.
     *
     * <p>Краткосрочное хранение быстро устаревающих данных:</p>
     * <ul>
     *   <li>Максимум 100 городов</li>
     *   <li>Время жизни 2 минуты</li>
     *   <li>Быстрое освобождение памяти</li>
     *   <li>Асинхронный режим для reactive операций</li>
     * </ul>
     *
     * @return кэш менеджер для погодных данных
     */
    @Bean("weatherCacheManager")
    @ConditionalOnProperty(name = "cache.weather.enabled", havingValue = "true", matchIfMissing = false)
    public CacheManager weatherCacheManager() {
        log.info("Configuring specialized Weather Cache Manager with async support");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager("weather");
        cacheManager.setAsyncCacheMode(true); // Включаем асинхронный режим
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(2))
            .recordStats()
            .removalListener((key, value, cause) ->
                log.debug("Weather cache entry expired: city={}", key))
        );

        return cacheManager;
    }

    /**
     * Высокопроизводительный кэш для частых операций.
     *
     * <p>Оптимизирован для высокой нагрузки:</p>
     * <ul>
     *   <li>Большой размер (5000 записей)</li>
     *   <li>Короткое время жизни (1 минута)</li>
     *   <li>Агрессивное освобождение памяти</li>
     *   <li>Асинхронный режим для reactive операций</li>
     * </ul>
     *
     * @return высокопроизводительный кэш менеджер
     */
    @Bean("highPerformanceCacheManager")
    @ConditionalOnProperty(name = "cache.high-performance.enabled", havingValue = "true", matchIfMissing = false)
    public CacheManager highPerformanceCacheManager() {
        log.info("Configuring High Performance Cache Manager with async support");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setAsyncCacheMode(true); // Включаем асинхронный режим
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofMinutes(1))
            .expireAfterAccess(Duration.ofSeconds(30))
            .recordStats()
            .removalListener((key, value, cause) -> {
                if (cause.wasEvicted()) {
                    log.debug("High performance cache eviction: key={}, cause={}", key, cause);
                }
            })
        );

        return cacheManager;
    }
}
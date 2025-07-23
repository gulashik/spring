package org.gualsh.demo.gw.config.gateway.limiter;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // Алгоритм работы (Token Bucket)
        //   1. Корзина токенов вмещает максимум 20 токенов
        //   2. Пополнение: каждую секунду добавляется 10 токенов
        //   3. Обработка запроса: тратится 1 токен
        //   4. Если токенов нет → запрос отклоняется с HTTP 429 (Too Many Requests)
        return new RedisRateLimiter(
            10, // replenishRate - базовая скорость пополнения токенов (10 токенов в секунду)
            20, // burstCapacity - максимальное количество токенов в корзине (20 токенов)
            10   // requestedTokens - каждый запрос "стоит" 1 токен
        );
    }
}

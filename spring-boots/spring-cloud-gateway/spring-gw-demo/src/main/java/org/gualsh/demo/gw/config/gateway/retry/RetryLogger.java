package org.gualsh.demo.gw.config.gateway.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class RetryLogger {

    public Mono<Void> logRetryAttempt(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        exchange.getAttributes().put("request-id", requestId);

        AtomicInteger attemptCounter = new AtomicInteger(1);

        return chain.filter(exchange)
            .doOnSubscribe(subscription -> {
                log.info("[{}] Starting request: {} {}",
                    requestId,
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI());
            })
            .doOnSuccess(aVoid -> {
                log.info("[{}] Request completed successfully on attempt #{}",
                    requestId, attemptCounter.get());
            })
            .doOnError(throwable -> {
                int currentAttempt = attemptCounter.getAndIncrement();
                log.warn("[{}] Attempt #{} failed: {} - {}",
                    requestId,
                    currentAttempt,
                    throwable.getClass().getSimpleName(),
                    throwable.getMessage());
            })
            .retryWhen(reactor.util.retry.Retry.backoff(3, Duration.ofMillis(10))
                .maxBackoff(Duration.ofMillis(50))
                .doBeforeRetry(retrySignal -> {
                    log.info("[{}] Preparing retry #{} with backoff delay",
                        requestId, retrySignal.totalRetries() + 1);
                })
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    log.error("[{}] All retry attempts exhausted after {} tries",
                        requestId, retrySignal.totalRetries() + 1);
                    return retrySignal.failure();
                })
            );
    }
}


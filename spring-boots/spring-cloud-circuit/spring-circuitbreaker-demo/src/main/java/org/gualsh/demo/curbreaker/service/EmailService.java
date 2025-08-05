package org.gualsh.demo.curbreaker.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.curbreaker.model.EmailRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для отправки email через Circuit Breaker.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Email сервисы часто имеют проблемы с производительностью и доступностью:
 * rate limiting от провайдеров, network issues, authentication problems.
 * Circuit Breaker помогает избежать блокировки приложения при проблемах с email.
 * </p>
 *
 * <p><strong>Типичные проблемы email сервисов:</strong></p>
 * <ul>
 *   <li>Rate limiting от SMTP провайдеров</li>
 *   <li>Authentication failures</li>
 *   <li>Network timeouts</li>
 *   <li>Quota exceeded errors</li>
 * </ul>
 *
 * <p><strong>Стратегии fallback для email:</strong></p>
 * <ul>
 *   <li>Queuing для последующей отправки</li>
 *   <li>Alternative delivery channels (SMS, push notifications)</li>
 *   <li>Simplified email templates</li>
 *   <li>Local storage для retry</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * EmailRequest request = EmailRequest.builder()
 *     .to("user@example.com")
 *     .subject("Test")
 *     .body("Test message")
 *     .from("sender@example.com")
 *     .build();
 *
 * boolean sent = emailService.sendEmail(request);
 * }</pre>
 *
 * @author Educational Demo
 * @see CircuitBreaker
 * @see EmailRequest
 */
@Slf4j
@Service
public class EmailService {

    private final CircuitBreaker circuitBreaker;

    /**
     * Конструктор с явным указанием @Qualifier для Circuit Breaker.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Lombok @RequiredArgsConstructor НЕ УМЕЕТ корректно обрабатывать @Qualifier аннотации.
     * Поэтому для dependency injection с qualifiers необходимо использовать явные конструкторы.
     * </p>
     *
     * @param circuitBreaker Circuit Breaker для email сервиса
     */
    public EmailService(@Qualifier("emailServiceCircuitBreaker") CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Отправка email с Circuit Breaker защитой.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Метод демонстрирует использование Circuit Breaker для защиты от проблем
     * с внешними email провайдерами. При срабатывании Circuit Breaker email
     * сохраняется в очередь для последующей отправки.
     * Fallback обрабатывается через try-catch, так как executeSupplier не имеет recover().
     * </p>
     *
     * <p><strong>Важные аспекты:</strong></p>
     * <ul>
     *   <li>Валидация входных данных перед отправкой</li>
     *   <li>Graceful fallback с queuing</li>
     *   <li>Различные типы ошибок и их обработка</li>
     *   <li>Логирование для audit trail</li>
     * </ul>
     *
     * @param emailRequest запрос на отправку email
     * @return true если отправлен успешно или поставлен в очередь
     */
    public boolean sendEmail(EmailRequest emailRequest) {
        log.debug("Отправка email: {} -> {}", emailRequest.getFrom(), emailRequest.getTo());

        // Валидация не должна проходить через Circuit Breaker
        if (!isValidEmailRequest(emailRequest)) {
            log.warn("Некорректный email запрос: {}", emailRequest);
            return false;
        }

        try {
            return circuitBreaker.executeSupplier(() -> {
                // Симуляция отправки email
                simulateEmailSending(emailRequest);

                log.info("Email успешно отправлен: {} -> {} [{}]",
                    emailRequest.getFrom(),
                    emailRequest.getTo(),
                    emailRequest.getSubject());

                return true;
            });
        } catch (Exception e) {
            log.warn("Email Circuit Breaker fallback для {}: {}",
                emailRequest.getTo(),
                e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());

            // Fallback: сохраняем в очередь для последующей отправки
            return queueEmailForLaterDelivery(emailRequest);
        }
    }

    /**
     * Отправка уведомления администратору с повышенным приоритетом.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Критически важные уведомления могут требовать специальной обработки.
     * Можно использовать отдельный Circuit Breaker или bypass для критических сообщений.
     * </p>
     *
     * @param message сообщение для администратора
     * @return true если отправлено успешно
     */
    public boolean sendAdminNotification(String message) {
        EmailRequest adminEmail = EmailRequest.builder()
            .to("admin@example.com")
            .from("system@example.com")
            .subject("Системное уведомление - " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME))
            .body(message)
            .priority("HIGH")
            .build();

        return sendEmail(adminEmail);
    }

    /**
     * Bulk отправка email с Circuit Breaker защитой.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Bulk операции требуют особого внимания при использовании Circuit Breaker.
     * Важно не допустить ситуации, когда из-за одного проблемного email
     * блокируется вся bulk операция.
     * </p>
     *
     * @param emails список email для отправки
     * @return количество успешно отправленных emails
     */
    public int sendBulkEmails(java.util.List<EmailRequest> emails) {
        log.info("Bulk отправка {} emails", emails.size());

        int successCount = 0;

        for (EmailRequest email : emails) {
            try {
                if (sendEmail(email)) {
                    successCount++;
                }

                // Небольшая задержка между отправками для соблюдения rate limits
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Bulk отправка прервана");
                break;
            } catch (Exception e) {
                log.error("Ошибка при bulk отправке email {}: {}", email.getTo(), e.getMessage());
            }
        }

        log.info("Bulk отправка завершена: {}/{} успешно", successCount, emails.size());
        return successCount;
    }

    /**
     * Проверка состояния email сервиса.
     *
     * @return true если сервис доступен
     */
    public boolean isEmailServiceHealthy() {
        try {
            return circuitBreaker.executeSupplier(() -> {
                // Простая проверка доступности
                simulateEmailServiceHealthCheck();
                return true;
            });
        } catch (Exception e) {
            log.warn("Email service health check failed: {}",
                e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Валидация email запроса.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Валидация бизнес-логики должна происходить ДО Circuit Breaker,
     * так как ошибки валидации не являются техническими проблемами сервиса.
     * </p>
     *
     * @param emailRequest запрос для валидации
     * @return true если запрос валиден
     */
    private boolean isValidEmailRequest(EmailRequest emailRequest) {
        return emailRequest != null &&
            emailRequest.getTo() != null && !emailRequest.getTo().trim().isEmpty() &&
            emailRequest.getFrom() != null && !emailRequest.getFrom().trim().isEmpty() &&
            emailRequest.getSubject() != null && !emailRequest.getSubject().trim().isEmpty() &&
            emailRequest.getBody() != null && !emailRequest.getBody().trim().isEmpty();
    }

    /**
     * Симуляция отправки email с возможными ошибками.
     *
     * @param emailRequest запрос на отправку
     */
    private void simulateEmailSending(EmailRequest emailRequest) {
        try {
            // Симуляция времени отправки (100-500ms нормально)
            int delay = ThreadLocalRandom.current().nextInt(100, 500);
            Thread.sleep(delay);

            // Симуляция случайных ошибок (8% вероятность)
            if (ThreadLocalRandom.current().nextDouble() < 0.08) {
                double errorType = ThreadLocalRandom.current().nextDouble();

                if (errorType < 0.3) {
                    throw new RuntimeException("SMTP authentication failed");
                } else if (errorType < 0.6) {
                    throw new RuntimeException("Rate limit exceeded");
                } else if (errorType < 0.8) {
                    throw new RuntimeException("SMTP server timeout");
                } else {
                    throw new RuntimeException("Invalid recipient address");
                }
            }

            // Симуляция медленной отправки (3% вероятность)
            if (ThreadLocalRandom.current().nextDouble() < 0.03) {
                log.warn("Медленная отправка email для: {}", emailRequest.getTo());
                Thread.sleep(12000); // больше timeout Circuit Breaker
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email sending interrupted", e);
        }
    }

    /**
     * Симуляция health check email сервиса.
     */
    private void simulateEmailServiceHealthCheck() {
        try {
            Thread.sleep(50);

            // Иногда health check может падать
            if (ThreadLocalRandom.current().nextDouble() < 0.05) {
                throw new RuntimeException("Email service health check failed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Health check interrupted", e);
        }
    }

    /**
     * Fallback: сохранение email в очередь для последующей отправки.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * В реальном приложении здесь был бы код для сохранения в Redis, RabbitMQ,
     * или другую очередь сообщений. Это обеспечивает eventual delivery.
     * </p>
     *
     * @param emailRequest email для сохранения в очередь
     * @return true если сохранен в очередь
     */
    private boolean queueEmailForLaterDelivery(EmailRequest emailRequest) {
        try {
            // Симуляция сохранения в очередь
            log.info("Email добавлен в очередь для последующей отправки: {} -> {}",
                emailRequest.getFrom(), emailRequest.getTo());

            // В реальном приложении:
            // messageQueue.send(emailRequest);
            // или
            // emailRepository.saveForRetry(emailRequest);

            return true;
        } catch (Exception e) {
            log.error("Ошибка при сохранении email в очередь: {}", e.getMessage());
            return false;
        }
    }
}
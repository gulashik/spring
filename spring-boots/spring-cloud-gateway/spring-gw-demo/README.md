# Spring Cloud Gateway Demo

```bash
# Запуск Redis контейнера
podman rm -f redis-gateway-demo

podman run -d \
  --name redis-gateway-demo \
  -p 6379:6379 \
  redis:7-alpine \
  redis-server --appendonly yes
  
podman ps 
```

```bash
# Логи 
podman logs redis-gateway-demo
```

```bash
# Завершаем, если запущено 
clear 
JPID=$(jps | grep GatewayDemoApplication | cut -d ' ' -f 1)
echo $JPID
if [ -n "$JPID" ]; then
    kill $JPID
fi
```

```
# Сборка и запуск приложения
mvn clean install spring-boot:run
```

```bash
# Проверка работоспособности
clear
curl -s http://localhost:8080/actuator/health | jq
```
```bash
# Маршруты
clear
curl -s http://localhost:8080/actuator/gateway/routes | jq
```

```bash
# Базовый маршрут
clear
curl  -X GET http://localhost:8080/demo/get
```

```bash
# Программный маршрут
clear
curl -X GET http://localhost:8080/programmatic/get
```

```bash
# Модификация заголовков
curl -v -X GET http://localhost:8080/transform/get
```

```bash
# Модификация заголовков
curl -X GET http://localhost:8080/transform-yml/get
```

```bash
# Проверка блокировки заголовков - Заблокирован
curl -X GET http://localhost:8080/request-info-filter/get
```

```bash
# Проверка блокировки заголовков - Заблокирован
clear
curl -X GET http://localhost:8080/request-block-filter/get 
```
```bash
# Проверка блокировки заголовков - Пройдёт т.к. все условия соблюдается
clear
curl -X GET http://localhost:8080/request-block-filter/get \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

```bash
# Проверка глобального фильтра - Заблокирован
clear
curl -X GET http://localhost:8080/admin/get
```

```bash
# Маршрут с условиями
clear
curl -X GET "http://localhost:8080/conditional/get?env=dev"
```

```bash
# Rate limiting
clear
curl -v -X GET http://localhost:8080/rate-limited/get
```

```bash
# Circuit breaker проверка работоспособности
clear
curl -X GET http://localhost:8080/circuit-breaker/get
```
```bash
# Проверка метрик Circuit Breaker
clear
curl -s http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state | jq
```
```bash 
#!/bin/bash
echo "Текущее состояние Circuit Breaker 'demo-circuit-breaker':"

states=("closed" "open" "half_open" "disabled" "forced_open" "metrics_only")

for state in "${states[@]}"; 
do
  value=$(curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:demo-circuit-breaker&tag=state:$state" | jq -r '.measurements[0].value')
  if [ "$value" != "0" ] && [ "$value" != "0.0" ] && [ "$value" != "null" ]; then
    clear
    echo "✅ Состояние: $state (значение: $value)"
  fi
done
```
```bash
# Все метрики Circuit Breaker
curl -s http://localhost:8080/actuator/metrics | grep circuitbreaker | jq
```
```bash 
# Prometheus метрики
clear
curl -s http://localhost:8080/actuator/prometheus | grep circuitbreaker
```
```bash 
# Prometheus метрики
clear
curl -s http://localhost:8080/actuator/prometheus | grep "resilience4j_circuitbreaker_state"
```

```bash
# Фильтры
curl http://localhost:8080/actuator/gateway/globalfilters | jq
```

#### 3. Rate Limiting не работает

**Проблема**: Запросы не ограничиваются

**Решение**: Убедитесь, что:
- Redis доступен и работает
- Конфигурация Redis корректна
- Rate limiter правильно настроен

#### 4. Маршруты не применяются

**Проблема**: Запросы не попадают в ожидаемые маршруты

**Решение**: Проверьте:
- Порядок маршрутов (более специфичные должны быть первыми)
- Предикаты правильно настроены
- Нет конфликтующих маршрутов

### Отладка

#### Включение debug логов

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: DEBUG
```

#### Проверка маршрутов

```bash
curl http://localhost:8080/actuator/gateway/routes | jq
```

#### Мониторинг Redis

```bash
redis-cli monitor
```

## 🔍 Образовательные моменты

### Reactive Programming

Gateway основан на Project Reactor. Ключевые концепции:
- `Mono<T>` - 0 или 1 элемент
- `Flux<T>` - 0 или много элементов
- Неблокирующие операции
- Backpressure

### Netty

Понимание Netty важно для:
- Настройки производительности
- Отладки сетевых проблем
- Оптимизации ресурсов

### Микросервисная архитектура

Gateway демонстрирует паттерны:
- API Gateway
- Service Discovery
- Circuit Breaker
- Rate Limiting
- Distributed Tracing

## 📚 Дополнительные ресурсы

### Документация
- [Spring Cloud Gateway Reference](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Project Reactor Documentation](https://projectreactor.io/docs)

### Примеры использования
- [Spring Cloud Samples](https://github.com/spring-cloud/spring-cloud-gateway)
- [Reactive Programming Guide](https://github.com/reactor/reactor-core)

## 🤝 Участие в проекте

Этот проект создан для образовательных целей. Вклад приветствуется:

1. Fork проекта
2. Создайте feature branch
3. Внесите изменения
4. Добавьте тесты
5. Обновите документацию
6. Создайте Pull Request

## 📄 Лицензия

Этот проект распространяется под MIT лицензией. См. файл LICENSE для подробностей.

## 📧 Контакты

Для вопросов и предложений создайте Issue в репозитории проекта.

---

**Важно**: Этот проект предназначен для образовательных целей. Для production использования необходимо адаптировать конфигурацию под конкретные требования безопасности и производительности.
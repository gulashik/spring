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
# C кастомным фильтром
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
curl -v -X GET http://localhost:8080/admin/get
```

```bash
# Маршрут с условиями - попадёт под условия
clear
curl -v -X GET "http://localhost:8080/conditional/get?env=dev"
```
```bash
# Маршрут с условиями - будет с Fallback т.е. X-Gateway-Fallback: true
clear
curl -v -X GET "http://localhost:8080/conditional/get?env=xxx"
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
# Circuit breaker проверка работоспособности
# Медленный запрос (>2 секунд согласно конфигурации). Откроется.
clear
curl -X GET http://localhost:8080/circuit-breaker/delay/3
curl -X GET http://localhost:8080/circuit-breaker/delay/3
curl -X GET http://localhost:8080/circuit-breaker/delay/3
curl -X GET http://localhost:8080/circuit-breaker/delay/3
curl -X GET http://localhost:8080/circuit-breaker/delay/3
```
```bash
# Общая информация о всех Circuit Breaker-ах будет статус OPEN
clear
curl -s http://localhost:8080/actuator/circuitbreakers | jq
```
```bash
# Cобытия Circuit Breaker-а
clear
curl -s http://localhost:8080/actuator/circuitbreakerevents | jq
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
#!/bin/bash
# Тестирование распределёния нагрузки
clear
REQUESTS=50
GATEWAY_URL="http://localhost:8080/weighted/get"

echo "Тестирование весового балансировщика ($REQUESTS запросов)..."
echo "Ожидаемое распределение: ~80% httpbin.org, ~20% postman-echo.com"
echo "================================================"

httpbin_count=0
postman_count=0

for i in $(seq 1 $REQUESTS); do
  response=$(curl -s -H "X-Test-Request: $i" "$GATEWAY_URL")
  
  if echo "$response" | grep -q "httpbin.org"; then
    httpbin_count=$((httpbin_count + 1))
    echo "Request $i: httpbin.org"
  elif echo "$response" | grep -q "postman-echo.com"; then
    postman_count=$((postman_count + 1))
    echo "Request $i: postman-echo.com"
  else
    echo "Request $i: Unknown backend"
  fi

  sleep 0.1
done

echo "================================================"
echo "Результаты тестирования:"
echo "httpbin.org: $httpbin_count запросов ($(( httpbin_count * 100 / REQUESTS ))%)"
echo "postman-echo.com: $postman_count запросов ($(( postman_count * 100 / REQUESTS ))%)"
echo "Всего: $((httpbin_count + postman_count)) из $REQUESTS запросов"
```

```bash 
# Тестирование запрос с ограничением по времени
#   увидим "X-Time-Based": "active" если время попадает в диапазон
#   увидим X-Gateway-Fallback: true если время НЕ попадает в диапазон
clear
curl -v -X GET "http://localhost:8080/time/get"
```

```bash
# Тестирование Retry механизма
# Успешный запрос (без повторов)
clear
curl -v -X GET http://localhost:8080/retry/get
```

```bash
# Тестирование Retry механизма
# Тестирование повторов при ошибке 502 (BAD_GATEWAY)
# httpbin.org/status/502 всегда возвращает 502 ошибку 
# В debug для r.n.http.client.HttpClientOperations видим что операции повторяются.
clear
curl -v -X GET http://localhost:8080/retry/status/502
```

```bash
# Тестирование Retry механизма
# Тестирование повторов при ошибке 504 (GATEWAY_TIMEOUT)
# В debug для r.n.http.client.HttpClientOperations видим что операции повторяются.
clear
curl -v -X GET http://localhost:8080/retry/status/504
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
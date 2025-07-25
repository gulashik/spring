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
curl -X GET http://localhost:8080/admin/get
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
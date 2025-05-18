#!/bin/bash

# Скрипт для демонстрации возможностей Micrometer с использованием API
# Выполняет серию тестовых запросов к эндпоинтам приложения

# Настройки
BASE_URL="http://localhost:8080"
DELAY=2  # Задержка между запросами в секундах

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}===================================================${NC}"
echo -e "${BLUE}      Демонстрация Micrometer API - Запросы${NC}"
echo -e "${BLUE}===================================================${NC}\n"

# Функция для выполнения запроса и отображения результата
function make_request() {
    local method=$1
    local endpoint=$2
    local description=$3
    local data=$4

    echo -e "${YELLOW}>> $description${NC}"
    echo -e "${GREEN}$method $BASE_URL$endpoint${NC}"

    if [ "$method" == "GET" ]; then
        response=$(curl -s -X GET "$BASE_URL$endpoint")
    elif [ "$method" == "POST" ]; then
        if [ -z "$data" ]; then
            response=$(curl -s -X POST "$BASE_URL$endpoint")
        else
            response=$(curl -s -X POST -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
        fi
    fi

    echo "$response" | python -m json.tool 2>/dev/null || echo "$response"
    echo -e "${BLUE}--------------------------------------------------${NC}\n"
    sleep $DELAY
}

# 1. Проверка статуса
make_request "GET" "/api/status" "Проверка статуса приложения (демонстрация @Timed)"

# 2. Информация о ресурсах
make_request "GET" "/api/resources" "Получение информации о ресурсах (демонстрация ручного Timer)"

# 3. Создание заказа
make_request "POST" "/api/orders" "Создание заказа (демонстрация бизнес-метрик)" '{"amount": 450.75, "region": "eu"}'

# 4. Генерация ошибки
make_request "GET" "/api/error/runtime" "Генерация ошибки для тестирования (runtime error)"

# 5. Метрики - получение списка
make_request "GET" "/api/metrics/list" "Получение списка всех доступных метрик"

# 6. Метрики - поиск по префиксу
make_request "GET" "/api/metrics/search?prefix=jvm" "Поиск метрик с префиксом 'jvm'"

# 7. Метрики - информация о конкретной метрике
make_request "GET" "/api/metrics/info?name=jvm.memory.used" "Получение информации о конкретной метрике"

# 8. Метрики - сводная информация
make_request "GET" "/api/metrics/summary" "Получение сводной информации о метриках"

# 9. Трассировка - демо
make_request "GET" "/api/tracing/demo" "Демонстрация трассировки"

# 10. Трассировка - операция
make_request "GET" "/api/tracing/operation?operationName=test-operation" "Выполнение пользовательской операции с трассировкой"

# 11. Трассировка - ресурс
make_request "GET" "/api/tracing/resource/12345" "Получение ресурса с трассировкой"

# 12. Счетчики - операция
make_request "POST" "/api/counters/operation?value=25" "Выполнение операции с счетчиками"

# 13. Счетчики - подсчет
make_request "GET" "/api/counters/count/api" "Демонстрация @Counted"

# 14. Счетчики - обработка
make_request "GET" "/api/counters/process?parameter=test" "Демонстрация комбинации @Timed и @Counted"

# 15. Счетчики - динамический
make_request "POST" "/api/counters/dynamic/test-category" "Создание динамического счетчика"

# 16. Генерация нагрузки (короткая)
make_request "GET" "/api/load-test?seconds=5" "Генерация небольшой нагрузки для тестирования метрик"

echo -e "${BLUE}===================================================${NC}"
echo -e "${BLUE}            Демонстрация завершена${NC}"
echo -e "${BLUE}===================================================${NC}\n"

echo -e "${GREEN}Теперь вы можете перейти к следующим ресурсам:${NC}"
echo -e "1. Actuator endpoints: ${YELLOW}$BASE_URL/actuator${NC}"
echo -e "2. Prometheus metrics: ${YELLOW}$BASE_URL/actuator/prometheus${NC}"
echo -e "3. Prometheus UI: ${YELLOW}http://localhost:9090${NC}"
echo -e "4. Grafana dashboards: ${YELLOW}http://localhost:3000${NC}"
echo -e "5. Zipkin tracing: ${YELLOW}http://localhost:9411${NC}\n"
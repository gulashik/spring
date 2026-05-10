## Spring Boot Starter для дополнительных Postgres-источников

## Структура:
```
.
├── compose.yml                    # три независимых Postgres (dwh, dictionary, history)
├── additional-sources-postgres/   # сам стартер 
└── dwh/                           # Spring Boot приложение, использующее стартер
```

---
## Файлы для демонстрации:
[ACTIONS.md](dwh/ACTIONS.md)
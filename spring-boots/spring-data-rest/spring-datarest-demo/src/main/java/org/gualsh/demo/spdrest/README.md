# Проект для демонстрации по Spring Data Rest

## Описание проекта

Этот проект демонстрирует использование Spring Data REST для автоматического создания REST API на основе репозиториев Spring Data JPA. 
Приложение представляет собой простую библиотечную систему с сущностями Author (Автор), Book (Книга) и Category (Категория).

## Структура проекта

```
src/main/java/org/gualsh/demo/spdrest/
├── SpringDataRestDemoApplication.java    # Главный класс приложения
├── config/                               # Конфигурационные классы
│   ├── JpaConfig.java                    # Конфигурация JPA
│   ├── OpenApiConfig.java                # Конфигурация OpenAPI
│   ├── SpringDataRestConfig.java         # Конфигурация Spring Data REST
│   └── DataInitializer.java              # Инициализация тестовых данных
├── controller/                           # Контроллеры
│   ├── CustomBookController.java         # Расширение функциональности REST API
│   └── HomeController.java               # Контроллер главной страницы
├── eventhandler/                         # Обработчики событий
│   └── BookEventHandler.java             # Обработчик событий для книг
├── model/                                # Модели данных
│   ├── Author.java                       # Модель автора
│   ├── Book.java                         # Модель книги
│   └── Category.java                     # Модель категории
├── projection/                           # Проекции для REST API
│   ├── AuthorWithBooks.java              # Проекция для автора с книгами
│   └── BookSummary.java                  # Проекция для книги
└── repository/                           # Репозитории
    ├── AuthorRepository.java             # Репозиторий для автора
    ├── BookRepository.java               # Репозиторий для книги
    └── CategoryRepository.java           # Репозиторий для категории
```

## Использование API

После запуска приложения API будет доступно по адресу: [http://localhost:8080/api](http://localhost:8080/api)

### Swagger UI

Документация API доступна по адресу: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### H2 Console

Консоль H2 (для профиля dev) доступна по адресу: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

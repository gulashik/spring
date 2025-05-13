package org.gualsh.demo.spdrest.controller;

import org.gualsh.demo.spdrest.model.Book;
import org.gualsh.demo.spdrest.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Кастомный контроллер для расширения функциональности Spring Data REST.
 * <p>
 * Аннотация @RepositoryRestController - позволяет создавать или переопределять(имеет приоритет над) автоматически генерируемые из репозиториев контролеры.<p>
 * Важно перехватывается только авто генерируемый контроллер, но не созданные в ручную @RestController.
 * <p>
 * Аннотация @BasePathAwareController указывает, что контроллер осведомлен о базовом пути API и будет использовать его для своих эндпоинтов.
 */
@RepositoryRestController
@BasePathAwareController
public class CustomBookController {

    private final BookRepository bookRepository;

    @Autowired
    public CustomBookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Возвращает статистику по книгам, сгруппированную по языкам.
     * Доступно по URL: /api/books/stats/by-language
     *
     * @return статистика по языкам книг
     */
    @GetMapping("/books/stats/by-language")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getBookStatsByLanguage() {
        List<Book> books = bookRepository.findAll();

        Map<String, Long> statsByLanguage = books.stream()
            .filter(book -> book.getLanguage() != null && !book.getLanguage().isEmpty())
            .collect(Collectors.groupingBy(
                Book::getLanguage,
                Collectors.counting()
            ));

        return ResponseEntity.ok(statsByLanguage);
    }

    /**
     * Возвращает книги, отфильтрованные по нескольким параметрам.
     * Доступно по URL: /api/books/filter?language=X&publisher=Y&minPages=Z
     *
     * @param language язык книги (опционально)
     * @param publisher издательство (опционально)
     * @param minPages минимальное количество страниц (опционально)
     * @return отфильтрованный список книг
     */
    @GetMapping("/books/filter")
    @ResponseBody
    public ResponseEntity<CollectionModel<EntityModel<Book>>> filterBooks(
        @RequestParam(required = false) String language,
        @RequestParam(required = false) String publisher,
        @RequestParam(required = false) Integer minPages) {

        List<Book> books = bookRepository.findAll().stream()
            .filter(book -> language == null ||
                (book.getLanguage() != null && book.getLanguage().equalsIgnoreCase(language)))
            .filter(book -> publisher == null ||
                (book.getPublisher() != null && book.getPublisher().contains(publisher)))
            .filter(book -> minPages == null ||
                (book.getPageCount() != null && book.getPageCount() >= minPages))
            .collect(Collectors.toList());

        List<EntityModel<Book>> bookModels = books.stream()
            .map(book -> EntityModel.of(book))
            .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(bookModels));
    }

    /**
     * Пример кастомного действия для книги.
     * Доступно по URL: /api/books/{id}/recommend
     *
     * @param id идентификатор книги
     * @return список рекомендованных книг
     */
    @GetMapping("/books/{id}/recommend")
    @ResponseBody
    public ResponseEntity<CollectionModel<EntityModel<Book>>> getRecommendedBooks(@PathVariable Long id) {
        // Получаем исходную книгу
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        // Находим другие книги из той же категории
        // Используем Pageable для ограничения результатов
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> recommendedBooks = bookRepository.findByCategoryId(book.getCategory().getId(), pageable)
            .getContent()
            .stream()
            .filter(b -> !b.getId().equals(id)) // Исключаем исходную книгу
            .limit(5) // Ограничиваем результат 5 книгами
            .collect(Collectors.toList());

        List<EntityModel<Book>> bookModels = recommendedBooks.stream()
            .map(b -> EntityModel.of(b))
            .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(bookModels));
    }
}
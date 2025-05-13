package org.gualsh.demo.spdrest.config;

import org.gualsh.demo.spdrest.model.Author;
import org.gualsh.demo.spdrest.model.Book;
import org.gualsh.demo.spdrest.model.Category;
import org.gualsh.demo.spdrest.repository.AuthorRepository;
import org.gualsh.demo.spdrest.repository.BookRepository;
import org.gualsh.demo.spdrest.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

/**
 * Конфигурация для загрузки тестовых данных.
 *
 * Используется только в профиле разработки (dev).
 * Заполняет базу данных тестовыми данными при запуске приложения.
 */
@Configuration
@Profile("dev")
public class DataInitializer {

    /**
     * Создает CommandLineRunner для заполнения базы данных тестовыми данными.
     *
     * @param categoryRepository репозиторий для категорий
     * @param authorRepository репозиторий для авторов
     * @param bookRepository репозиторий для книг
     * @return CommandLineRunner для инициализации данных
     */
    @Bean
    public CommandLineRunner initData(
        CategoryRepository categoryRepository,
        AuthorRepository authorRepository,
        BookRepository bookRepository) {

        return args -> {
            // Создаем категории
            Category fiction = new Category();
            fiction.setName("Fiction");
            fiction.setDescription("Литературные произведения, созданные на основе воображения");
            categoryRepository.save(fiction);

            Category scienceFiction = new Category();
            scienceFiction.setName("Science Fiction");
            scienceFiction.setDescription("Жанр, в основе которого лежит фантастика, связанная с научными достижениями");
            categoryRepository.save(scienceFiction);

            Category nonFiction = new Category();
            nonFiction.setName("Non-Fiction");
            nonFiction.setDescription("Литературные произведения, основанные на реальных событиях и фактах");
            categoryRepository.save(nonFiction);

            Category programming = new Category();
            programming.setName("Programming");
            programming.setDescription("Книги о программировании и разработке");
            categoryRepository.save(programming);

            // Создаем авторов
            Author author1 = new Author();
            author1.setFirstName("Фёдор");
            author1.setLastName("Достоевский");
            author1.setBirthDate(LocalDate.of(1821, 11, 11));
            author1.setBiography("Русский писатель, мыслитель, философ и публицист");
            authorRepository.save(author1);

            Author author2 = new Author();
            author2.setFirstName("Айзек");
            author2.setLastName("Азимов");
            author2.setBirthDate(LocalDate.of(1920, 1, 2));
            author2.setBiography("Американский писатель-фантаст, популяризатор науки, биохимик");
            authorRepository.save(author2);

            Author author3 = new Author();
            author3.setFirstName("Роберт");
            author3.setLastName("Мартин");
            author3.setBirthDate(LocalDate.of(1952, 12, 5));
            author3.setBiography("Американский программист и автор книг по разработке ПО");
            authorRepository.save(author3);

            Author author4 = new Author();
            author4.setFirstName("Джошуа");
            author4.setLastName("Блох");
            author4.setBirthDate(LocalDate.of(1961, 8, 28));
            author4.setBiography("Американский программист, известный своей работой над Java");
            authorRepository.save(author4);

            // Создаем книги
            Book book1 = new Book();
            book1.setTitle("Преступление и наказание");
            book1.setIsbn("978-5-17-084918-7");
            book1.setPublicationDate(LocalDate.of(1866, 1, 1));
            book1.setPageCount(672);
            book1.setDescription("Роман о нравственных и психологических терзаниях убийцы");
            book1.setCategory(fiction);
            book1.setLanguage("Русский");
            book1.setPublisher("ACT");
            book1.addAuthor(author1);
            bookRepository.save(book1);

            Book book2 = new Book();
            book2.setTitle("Братья Карамазовы");
            book2.setIsbn("978-5-17-084919-4");
            book2.setPublicationDate(LocalDate.of(1880, 1, 1));
            book2.setPageCount(992);
            book2.setDescription("Последний роман Достоевского");
            book2.setCategory(fiction);
            book2.setLanguage("Русский");
            book2.setPublisher("ACT");
            book2.addAuthor(author1);
            bookRepository.save(book2);

            Book book3 = new Book();
            book3.setTitle("Основание");
            book3.setIsbn("978-5-699-68044-4");
            book3.setPublicationDate(LocalDate.of(1951, 5, 1));
            book3.setPageCount(432);
            book3.setDescription("Первый роман из цикла 'Основание'");
            book3.setCategory(scienceFiction);
            book3.setLanguage("Английский");
            book3.setPublisher("Эксмо");
            book3.addAuthor(author2);
            bookRepository.save(book3);

            Book book4 = new Book();
            book4.setTitle("Чистый код");
            book4.setIsbn("978-5-4461-0960-0");
            book4.setPublicationDate(LocalDate.of(2008, 8, 1));
            book4.setPageCount(464);
            book4.setDescription("Создание, анализ и рефакторинг кода");
            book4.setCategory(programming);
            book4.setLanguage("Английский");
            book4.setPublisher("Питер");
            book4.addAuthor(author3);
            bookRepository.save(book4);

            Book book5 = new Book();
            book5.setTitle("Effective Java");
            book5.setIsbn("978-5-6040723-5-1");
            book5.setPublicationDate(LocalDate.of(2018, 1, 1));
            book5.setPageCount(464);
            book5.setDescription("Лучшие практики программирования на Java");
            book5.setCategory(programming);
            book5.setLanguage("Английский");
            book5.setPublisher("Диалектика");
            book5.addAuthor(author4);
            bookRepository.save(book5);
        };
    }
}
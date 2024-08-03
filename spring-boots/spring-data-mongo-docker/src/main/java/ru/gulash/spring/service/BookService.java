package ru.gulash.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gulash.spring.domain.Author;
import ru.gulash.spring.domain.Book;
import ru.gulash.spring.repostory.AuthorRepository;
import ru.gulash.spring.repostory.BookRepository;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    // Метод для добавления книги с автором
    public Book addBookWithAuthor(String title, String genre, String authorName, String authorBio) {
        Author author = new Author(authorName, authorBio);
        authorRepository.save(author);  // Сохраняем автора

        Book book = new Book(title, genre, author);  // Создаем книгу с ссылкой на автора
        return bookRepository.save(book);  // Сохраняем книгу
    }

    // Метод для получения книги по её ID
    public Book getBookById(String bookId) {
        return bookRepository.findById(bookId).orElse(null);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
}

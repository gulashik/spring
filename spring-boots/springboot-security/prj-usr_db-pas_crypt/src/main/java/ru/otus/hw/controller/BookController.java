package ru.otus.hw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.model.dto.BookCreateDto;
import ru.otus.hw.model.dto.BookDto;
import ru.otus.hw.model.dto.BookUpdateDto;
import ru.otus.hw.service.BookService;

@Controller
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books";
    }

    @GetMapping("/edit")
    public String editBook(@RequestParam("id") Integer id, Model model) {
        BookDto byId = bookService.findById(id);
        model.addAttribute("book", byId);
        return "edit";
    }

    @PostMapping("/edit")
    public String editBook(@ModelAttribute("book") BookUpdateDto book) {
        bookService.update(book);
        return "redirect:/";
    }

    @GetMapping("/add")
    public String insertBook(Model model) {
        model.addAttribute("book", new BookUpdateDto(0L, "some title", 1L, 1L));
        return "add";
    }

    @PostMapping("/add")
    public String insertBook(@ModelAttribute("book") BookCreateDto book) {
        bookService.insert(book);
        return "redirect:/";
    }

    @GetMapping("/delete")
    public String deleteBook(@RequestParam("id") Integer id) {
        bookService.deleteById(id);
        return "redirect:/";
    }
}

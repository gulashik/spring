package ru.gulash.spring;

import com.github.cloudyrock.spring.v5.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import ru.gulash.spring.domain.Person;
import ru.gulash.spring.domain.Post;
import ru.gulash.spring.domain.User;
import ru.gulash.spring.repostory.PersonRepository;
import ru.gulash.spring.repostory.PostRepository;
import ru.gulash.spring.service.BookService;
import ru.gulash.spring.service.PostService;
import ru.gulash.spring.service.UserService;

import java.util.List;

@EnableMongock // todo включить зависимость Mongock(миграция для mongodb)
@EnableMongoRepositories // todo включить
@SpringBootApplication
public class Main {
    /* todo запустить docker-compose.yml */
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class);

        PersonRepository personRepository = context.getBean(PersonRepository.class);

        personRepository.save(new Person("Dostoevsky", "Dostoevsky@mail.ru"));

        System.out.println("\n\n\n----------------------------------------------\n\n");
        System.out.println("Авторы в БД:");
        personRepository.findAll().forEach(p -> System.out.println(p.getName()));
        System.out.println("\n\n----------------------------------------------\n\n\n");

        //---------------
        PostService postService = context.getBean(PostService.class);
        postService.createPost();

        Post post = postService.findPost("post");
        post.setContent("changed");
        postService.save(post);

        List<Post> postList = context.getBean(PostRepository.class).findAll();

        System.out.println("\n\n\n----------------------------------------------\n\n");
        System.out.println("Posts");
        postList.forEach(System.out::println);
        System.out.println("\n\n\n----------------------------------------------\n\n");

        //---------------
        System.out.println("\n\n\n----------------------------------------------\n\n");
        BookService bookService = context.getBean(BookService.class);
        bookService.addBookWithAuthor(
            /*title*/ "book title",
            /*String genre*/ "my_genre",
            /*String authorName*/ "author1",
            /*String authorBio*/ "M"
        );
        bookService.getAllBooks()
            .forEach(System.out::println);
        System.out.println("\n\n\n----------------------------------------------\n\n");

        //---------------
        System.out.println("\n\n\n----------------------------------------------\n\n");
        UserService userService = context.getBean(UserService.class);

        List<User> johnDoe = userService.findUsersByName("John Doe");
        johnDoe.forEach(System.out::println);
        System.out.println("\n\n\n----------------------------------------------\n\n");
    }
}





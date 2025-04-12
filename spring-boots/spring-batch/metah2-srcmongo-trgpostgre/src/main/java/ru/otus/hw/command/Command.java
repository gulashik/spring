package ru.otus.hw.command;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.h2.tools.Console;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.model.targetdb.entity.Author;
import ru.otus.hw.model.targetdb.entity.Book;
import ru.otus.hw.model.targetdb.entity.Comment;
import ru.otus.hw.model.targetdb.entity.Genre;

import java.util.List;


@ShellComponent
public class Command {
    private final Job migrateJob;

    private final JobLauncher jobLauncher;

    private final EntityManager entityManager;

    public Command(
        Job migrateJob,
        JobLauncher jobLauncher,
        @Qualifier("postgresEntityManager") EntityManager entityManager
    ) {
        this.migrateJob = migrateJob;
        this.jobLauncher = jobLauncher;
        this.entityManager = entityManager;
    }

    @ShellMethod(value = "startMigration", key = "sm")
    public void startMigration() throws Exception {
        final JobExecution jobExecution = jobLauncher.run(migrateJob, new JobParameters());
        System.out.println(jobExecution);
    }

    @ShellMethod(value = "openConsoleH2", key = "oc")
    public String openConsoleH2() {
        try {
            Console.main();

            return """
                    Opening console H2 see application.yml
                        datasource:
                        url: jdbc:h2:mem:h2db
                        driver-class-name: org.h2.Driver
                        username: root
                        password: 
                    """;
        } catch (final Exception ex) {

            return "Error opening console H2: %s".formatted(ex.getLocalizedMessage());
        }
    }

    @ShellMethod(value = "ShowMigrationAuthor", key = "sma")
    public String showMigrationAuthor() {

        return getMigrationEntity("SELECT a FROM Author a", Author.class);
    }

    @ShellMethod(value = "ShowMigrationGenre", key = "smg")
    public String showMigrationGenre() {

        return getMigrationEntity("SELECT g FROM Genre g", Genre.class);
    }

    @ShellMethod(value = "ShowMigrationComment", key = "smc")
    public String showMigrationComment() {

        return getMigrationEntity("SELECT c FROM Comment c", Comment.class);
    }

    @ShellMethod(value = "ShowMigrationBook", key = "smb")
    public String showMigrationBook() {

        return getMigrationEntity("SELECT b FROM Book b", Book.class);
    }

    private <T> String getMigrationEntity(String queryText, Class<T> resultClass) {
        String entityName = resultClass.getSimpleName();

        try {
            TypedQuery<T> query = entityManager.createQuery(queryText, resultClass);
            List<T> obj = query.getResultList();

            if (obj.isEmpty()) {

                return "No %s found in database".formatted(entityName);
            }

            StringBuilder result = new StringBuilder("%s in database:\n".formatted(entityName));
            obj.forEach(author -> result.append(author).append("\n"));

            return result.toString();
        } catch (Exception e) {
            return "Error retrieving %s: ".formatted(entityName) + e.getMessage();
        }
    }
}
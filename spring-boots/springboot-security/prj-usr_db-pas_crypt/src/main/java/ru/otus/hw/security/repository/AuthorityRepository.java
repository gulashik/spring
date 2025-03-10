package ru.otus.hw.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.security.model.Authority;

/*todo JPA репозиторий*/
public interface AuthorityRepository
    extends JpaRepository<Authority/*todo сущность*/, Long> {
}

package ru.otus.hw.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.security.model.AuthenticatedUserDetails;


import java.util.Optional;

/*todo JPA репозиторий*/
public interface UserRepository
    extends JpaRepository<AuthenticatedUserDetails/*todo сущность*/, Long>
{
    Optional<AuthenticatedUserDetails> findByUsername(String username);
}

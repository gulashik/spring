package ru.otus.hw.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.security.model.AuthenticatedUserDetails;


import java.util.Optional;

public interface UserRepository extends JpaRepository<AuthenticatedUserDetails, Long> {
    Optional<AuthenticatedUserDetails> findByUsername(String username);
}

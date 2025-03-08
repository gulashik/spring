package ru.otus.hw.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.security.model.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
}

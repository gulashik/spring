package ru.otus.hw.security.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@Entity
@Table(name = "authorities")
/*todo роли пользователя*/
public class Authority implements GrantedAuthority /*todo наследуемся */{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "username", nullable = false)
    private AuthenticatedUserDetails user;

    @Column(name = "authority", nullable = false)
    private String authority;
}
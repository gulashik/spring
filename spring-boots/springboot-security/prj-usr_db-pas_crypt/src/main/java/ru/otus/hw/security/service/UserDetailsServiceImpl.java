package ru.otus.hw.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.security.model.AuthenticatedUserDetails;
import ru.otus.hw.security.model.Authority;
import ru.otus.hw.security.repository.UserRepository;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl
    /*todo имплементируем UserDetailsService или используем bean UserDetailsManager*/
    implements UserDetailsService
{
    private final UserRepository userRepository;

    // todo UserDetailsManager - удобная штатная имплементация UserDetailsService
    // An extension of the UserDetailsService which provides the ability to create new users and update existing ones.
    private final UserDetailsManager userDetailsManager;

    // todo работа с паролями
    private final PasswordEncoder passwordEncoder;

    // todo загрузка пользователя имплементация по UserDetailsService
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<AuthenticatedUserDetails> authenticatedUser = userRepository
            .findByUsername(username);

        return authenticatedUser.map(
            authenticatedUserDetails -> User
                .withUsername(authenticatedUserDetails.getUsername())
                .password(authenticatedUserDetails.getPassword())
                .authorities(
                    authenticatedUserDetails.getAuthorities().stream()
                        .map(Authority::getAuthority)
                        .toArray(String[]::new)
                )
                .build()
        ).orElse(null);
    }

    // todo создание пользователя
    @Transactional(rollbackFor = Exception.class)
    public UserDetails createUser(String username, String rawPassword, String... authorities) {

        if (userRepository.findByUsername(username).isEmpty()) {
            UserDetails user = User
                .builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .roles(authorities)
                .build();
            userDetailsManager.createUser(user);
        }

        return userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Something went wrong " + username));
    }
}
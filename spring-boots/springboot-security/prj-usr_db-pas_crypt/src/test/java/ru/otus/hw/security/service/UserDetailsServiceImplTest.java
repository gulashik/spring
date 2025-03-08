package ru.otus.hw.security.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.otus.hw.security.SecurityConfiguration;
import ru.otus.hw.security.model.AuthenticatedUserDetails;
import ru.otus.hw.security.model.Authority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Сервис на основе Jpa для работы с данными пользователя ")
@DataJpaTest
@Import(
    {
        UserDetailsServiceImpl.class,
        SecurityConfiguration.class
    }
)
class UserDetailsServiceImplTest {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    final String TEST_STRING = "test";
    final String RAW_PASSWORD = "testPass";

    @DisplayName("Должен корректно создавать и получать пользователя в БД")
    @Test
    void shouldCreateAndGetUserFromDB() {

        AuthenticatedUserDetails expectedUserDetails = new AuthenticatedUserDetails();
        expectedUserDetails.setUsername(TEST_STRING);
        expectedUserDetails.setPassword(passwordEncoder.encode(RAW_PASSWORD));

        Authority authority = new Authority();
        authority.setAuthority("ROLE_" + TEST_STRING.toUpperCase());

        expectedUserDetails.setAuthorities(Set.of(authority));

        // act
        UserDetails createdUserDetails = userDetailsService.createUser(
            TEST_STRING,
            RAW_PASSWORD,
            TEST_STRING.toUpperCase()
        );

        UserDetails actualUserDetails = userDetailsService
            .loadUserByUsername(
                expectedUserDetails.getUsername()
            );

        // assert
        // Actual user
        assertTrue(
            passwordEncoder.matches(RAW_PASSWORD, actualUserDetails.getPassword()),
            "Actual user passwords must be the same"
        );

        assertThat(actualUserDetails.getUsername())
            .as("Actual user username must be the same")
            .isEqualTo(expectedUserDetails.getUsername());

        List<String> actualAuthoritiesList = actualUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        List<String> excpectedAuthoritiesList = expectedUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(actualAuthoritiesList)
            .as("Actual user authorities must be the same")
            .containsExactlyElementsOf(excpectedAuthoritiesList);

        // Created user
        assertTrue(
            passwordEncoder.matches(RAW_PASSWORD, createdUserDetails.getPassword()),
            "Created user passwords must be the same"
        );

        assertThat(createdUserDetails.getUsername())
            .as("Created user username must be the same")
            .isEqualTo(expectedUserDetails.getUsername());

        List<String> createdAuthoritiesList = createdUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(createdAuthoritiesList)
            .as("Created user authorities must be the same")
            .containsExactlyElementsOf(excpectedAuthoritiesList);
    }
}
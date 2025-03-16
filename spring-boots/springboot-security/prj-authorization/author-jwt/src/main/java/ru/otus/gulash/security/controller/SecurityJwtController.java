package ru.otus.gulash.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;


@RestController
public class SecurityJwtController {

    JwtEncoder encoder;

    public SecurityJwtController(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    // todo генерируем JWT после Basic аутентификации(см. SecurityConfig)
    @GetMapping("/token")
    public String token( /*todo Authentication на вход*/Authentication authentication) {
        Instant now = Instant.now();
        long expiry = 36000L;

        var scope = authentication
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toArray();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self") // кто выпустил
            .issuedAt(now) // действует с
            .expiresAt(now.plusSeconds(expiry)) // действует по
            .subject(authentication.getName()) // user

            // todo claim - это пары ключ-значение, которые содержат информацию о субъекте и дополнительные метаданные.
            .claim("scope", scope) // scope - array ролей
            .claim("roles", new String[]{"JWT_USER", "JWT_ADMIN"}) // scope - роли через запятую
            .build();

        String tokenValue = this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        System.out.println("tokenValue: " + tokenValue);
        return tokenValue;
    }
}

package ru.otus.security.jwt.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.List;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    // todo Публичный ключ
    @Value("${jwt.public.key}")
    RSAPublicKey key;

    // todo Приватный ключ
    @Value("${jwt.private.key}")
    RSAPrivateKey priv;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // todo фильтр по URL-ам
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers(HttpMethod.POST, "/token").hasRole("APP")
                .requestMatchers("/userinfo").hasAnyAuthority("ROLE_JWT_ADMIN", "SCOPE_ROLE_APP")
                .anyRequest().authenticated()
            )
            .csrf(AbstractHttpConfigurer::disable)

            // todo basic авторизация для получения jwt
            .httpBasic(Customizer.withDefaults())

            // todo OAuth2 Resource Server для работы с JWT. В данном случае, используется JWT для аутентификации.
            //.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())) // todo можно использовать default потом парсить jwt
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        /*todo кастомный конвертер*/jwtAuthenticationConverter()
                    ))
            )
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // todo используются кастомные обработчики
            //  для ошибок аутентификации (BearerTokenAuthenticationEntryPoint) и
            //  доступа (BearerTokenAccessDeniedHandler).
            .exceptionHandling((exceptions) -> exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            );

        return http.build();
    }

    @Bean
    UserDetailsService users() {

        return new InMemoryUserDetailsManager(
            User.withUsername("user")
                .password("user")
                .authorities("ROLE_APP", "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_THE_BEST")
                .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                return charSequence.toString();
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return charSequence.toString().equals(s);
            }
        };
    }

    // todo работа с JWT
    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.key).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new CustomJwtGrantedAuthoritiesConverter());
        return jwtConverter;
    }
}

// todo Свой обработчик Claim-ов из JWT. Имеет смысл накидывать роли в GrantedAuthority
class CustomJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    // todo Доставалка GrantedAuthoritys из claim-а scope
    //  Extracts the GrantedAuthoritys from scope attributes typically found in a Jwt.
    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Вначале штатный конвертер. Добавит к claim scope префикс SCOPE_
        Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);

        // получаем роли из своего claim-ма roles
        if (jwt.getClaims().containsKey("roles")) {
            List<String> roles = jwt.getClaim("roles");
            authorities.addAll(
                roles
                    .stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList()
            );
        }
        return authorities;
    }
}
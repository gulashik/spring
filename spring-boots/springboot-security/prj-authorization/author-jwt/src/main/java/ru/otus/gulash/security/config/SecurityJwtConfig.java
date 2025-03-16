package ru.otus.gulash.security.config;

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
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.List;

@Configuration
public class SecurityJwtConfig {

    // todo Публичный ключ
    @Value("${jwt.public.key}")
    RSAPublicKey key;

    // todo Приватный ключ
    @Value("${jwt.private.key}")
    RSAPrivateKey priv;

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

    // todo Получаем конвертор с добавлением кастомной логики преобразования claim-ов из JWT
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();

        jwtConverter.setJwtGrantedAuthoritiesConverter(new CustomJwtGrantedAuthoritiesConverter());

        return jwtConverter;
    }

    // todo Свой обработчик Claim-ов из JWT. Имеет смысл накидывать роли в GrantedAuthority
    static class CustomJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        //  Доставалка GrantedAuthoritys из claim-а scope
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
}

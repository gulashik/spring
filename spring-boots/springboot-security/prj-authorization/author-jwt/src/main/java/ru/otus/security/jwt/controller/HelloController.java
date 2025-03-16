package ru.otus.security.jwt.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello( /*todo Authentication на вход*/Authentication authentication ) {
        return "Hello, " + authentication.getName() + "!";
    }


    @GetMapping("/userinfo")
    public String getUserInfo(
        /*todo Authentication на вход, так же будет содержать JWT в SecurityContextHolder.getContext().getAuthentication().getPrincipal()*/
        Authentication authentication,
        /*todo можно работать с JWT напрямую*/
        @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaim("sub");
        List<String> roles = jwt.getClaim("roles");
        List<String> authorities = jwt.getClaim("scope");

        StringBuilder res = new StringBuilder();
        res.append("From JWT. Username: ").append(username)
            .append(", Roles: ").append(roles)
            .append(", Authorities: ").append(authorities);

        res.append("\n");

        res.append("From Authentication. Username: ").append( ((Jwt)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaim("sub").toString() )
        .append(", Authorities: ").append(authentication.getAuthorities());

        System.out.println(res);
        return res.toString();
    }
}

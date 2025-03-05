package ru.otus.spring.rest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PagesController {

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/public")
    public String publicPage(@RequestParam(name = "SpecialValueExpect") String specialValue, @RequestParam(name = "SpecialValueExpect2") String specialValue2,Model model) {
        model.addAttribute("secret", specialValue);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        // todo проверка если пользователь анонимный
        if (authentication instanceof AnonymousAuthenticationToken) {
            System.out.println("Это анонимный пользователь!");
        }
        System.out.println( ((UserDetails)(authentication.getPrincipal())).getUsername() );
        System.out.println(authentication.getPrincipal());
        return "public";
    }

    @GetMapping("/authenticated")
    public String authenticatedPage(Model model) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        User user = (User) securityContext.getAuthentication().getPrincipal();
        model.addAttribute("userName", user.getUsername());
        return "authenticated";
    }

    @GetMapping("/success")
    public String successPage() {
        return "success";
    }

    @GetMapping("/error")
    public String errorPage(Model model) {
        model.addAttribute("source", "errorPage");
        return "error";
    }

    @PostMapping("/fail")
    public String failPage(Model model) {
        model.addAttribute("source", "failPage");
        return "error";
    }
}

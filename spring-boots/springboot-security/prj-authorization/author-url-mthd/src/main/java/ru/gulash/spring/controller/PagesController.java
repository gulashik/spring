package ru.gulash.spring.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.gulash.spring.service.MySecureService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PagesController {

    private final MySecureService mySecureService;

    public PagesController(MySecureService mySecureService) {
        this.mySecureService = mySecureService;
    }

    @GetMapping("/")
    public String indexPage() {
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return "index";
    }

    @GetMapping("/public")
    public String publicPage() {
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return "public";
    }

    @GetMapping("/user")
    public String userPage() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // todo PreFilter тест
        // todo нужная MUTABLE коллекция, т.к. она будет модифицироваться
        List<String> listBeforePre = new ArrayList<>(List.of("user", "admin", userDetails.getUsername()));
        System.out.println("listBeforePre: " + listBeforePre);
        List<String> listAfterPre = mySecureService.preFilter(listBeforePre); // используем @PreFilter
        System.out.println("listAfterPre: " + listAfterPre);
        /*
            listBeforePre: [user, admin, user]
            Income this method: [user, user]
            listAfterPre: [user, user]
        */

        // todo @PostFilter
        // todo нужная MUTABLE коллекция, т.к. она будет модифицироваться
        List<String> listBeforePost = new ArrayList<>(List.of("user", "admin", userDetails.getUsername()));
        // List<String> listBeforePost = List.of("user", "admin", userDetails.getUsername()); - immutable будет ошибка
        System.out.println("listBeforePost: " + listBeforePost);
        List<String> listAfterPost = mySecureService.postFilter(listBeforePost);
        System.out.println("listAfterPost: " + listAfterPost);
        /*
            listBeforePost: [user, admin, user]
            Income this method: [user, admin, user]
            listAfterPost: [user, user]
        */

        System.out.println(userDetails.getUsername());

        try {
            String s = mySecureService.onlyUser();
            System.out.println("random fired " + s);
        }
        catch (Exception e) {
            System.out.println("random not fired ");
            return "error";
        }

        try {
            List<String> validUsers = mySecureService.getValidUsers(0L);
            System.out.println("current user valid " + validUsers);
        }
        catch (Exception e) {
            System.out.println("current user not valid");
            return "error";
        }
        return "user";
    }

    @GetMapping("/admin")
    public String adminPage() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        System.out.println(userDetails.getUsername());

        mySecureService.onlyAdmin();
        return "admin";
    }

    @GetMapping("/manager")
    public String managerPage() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(userDetails.getUsername());
        return "manager";
    }

    @GetMapping("/authenticated")
    public String authenticatedPage() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(userDetails.getUsername());
        return "authenticated";
    }

    @GetMapping("/success")
    public String successPage() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(userDetails.getUsername());
        return "success";
    }
}

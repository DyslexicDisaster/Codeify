package codeify.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    /**
     * Handles requests to the registration page.
     *
     * @return the name of the Thymeleaf template for the registration page.
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * Handles requests to the login page.
     *
     * @return the name of the Thymeleaf template for the login page.
     */
    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/codeEditor")
    public String code() { return "codeEditor"; }

    /**
     * Handles requests to the home page.
     *
     * @return the name of the Thymeleaf template for the home page.
     */
    @GetMapping("/")
    public String home() { return "index"; }
}

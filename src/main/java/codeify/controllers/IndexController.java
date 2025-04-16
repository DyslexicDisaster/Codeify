package codeify.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping(value = {"/", "/login", "/register", "/codeEditor"})
    public String forwardToReact() {
        // Forward all frontend routes to React's index.html
        return "forward:/index.html";
    }
}
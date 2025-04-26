package codeify.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    /**

     This method is used to forward all requests to the React application.*
     @return a String representing the path to the index.html file.*/@GetMapping(value = {"/", "/login", "/register", "/codeEditor"})
    public String forwardToReact() {
        return "forward:/index.html";}
}
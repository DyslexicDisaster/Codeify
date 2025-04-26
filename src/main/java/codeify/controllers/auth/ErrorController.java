package codeify.controllers.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error handler for handling unmapped routes or unexpected errors in the Codeify application.
 */
@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // Redirect to your frontend error page
        return "redirect:http://localhost:3000/error";
    }
}
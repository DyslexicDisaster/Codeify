package codeify.controllers.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    /**
     * This method handles the error page.
     *
     * @return redirects to the error page of the frontend application.
     */
    @RequestMapping("/error")
    public String handleError() {
        return "redirect:http://localhost:3000/error";
    }
}
package codeify.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Custom error handler for handling unmapped routes or unexpected errors in the Codeify application.
 */
@Controller
public class ErrorHandler implements ErrorController {
    private final static String PATH = "/error";

    /**
     * Handles requests to the default error path.
     *
     * This method is invoked when no mapping is found for a request or an internal error occurs.
     *
     * @return a simple response message indicating that no mapping was found.
     */
    @RequestMapping(PATH)
    @ResponseBody
    public String getErrorPath() {
        return "No Mapping Found";
    }
}
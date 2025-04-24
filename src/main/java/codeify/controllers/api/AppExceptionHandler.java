package codeify.controllers.api;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * A centralized exception handler for the Codeify application.
 */
@ControllerAdvice
public class AppExceptionHandler {

    /**
     * Handles NullPointerException exceptions specifically.
     *
     * @param model the model object used to pass error details to the view.
     * @return the name of the error view template to display.
     */
    @ExceptionHandler(value = NullPointerException.class)
    public String nullPointerHandler(Model model){
        model.addAttribute("err", "NullPointerException");
        return "error";
    }

    /**
     * Handles all other exceptions that are not explicitly caught by more specific handlers.
     *
     * @param ex    the exception object that was thrown.
     * @return the name of the error view template to display.
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(Exception ex) {
        ModelAndView model = new ModelAndView("error");
        model.addObject("err", ex.getClass().getSimpleName());
        model.addObject("message", ex.getMessage());
        return model;
    }
}
package codeify.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * This method configures the exception resolvers for the application.
     * It adds a custom exception resolver to handle NoHandlerFoundException.
     *
     * @param resolvers the list of exception resolvers
     */
    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, new DefaultHandlerExceptionResolver() {
            @Override
            protected ModelAndView handleNoHandlerFoundException(
                    NoHandlerFoundException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No handler found for the request");
                return new ModelAndView();
            }
        });
    }
}
